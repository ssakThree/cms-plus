package kr.or.kosa.cmsplusmain.domain.billing.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import kr.or.kosa.cmsplusmain.domain.base.dto.PageReq;
import kr.or.kosa.cmsplusmain.domain.base.dto.PageRes;
import kr.or.kosa.cmsplusmain.domain.billing.dto.BillingCreateReq;
import kr.or.kosa.cmsplusmain.domain.billing.dto.BillingDetailRes;
import kr.or.kosa.cmsplusmain.domain.billing.dto.BillingListItemRes;
import kr.or.kosa.cmsplusmain.domain.billing.dto.BillingProductReq;
import kr.or.kosa.cmsplusmain.domain.billing.dto.BillingSearchReq;
import kr.or.kosa.cmsplusmain.domain.billing.dto.BillingUpdateReq;
import kr.or.kosa.cmsplusmain.domain.billing.entity.Billing;
import kr.or.kosa.cmsplusmain.domain.billing.entity.BillingProduct;
import kr.or.kosa.cmsplusmain.domain.billing.repository.BillingCustomRepository;
import kr.or.kosa.cmsplusmain.domain.billing.repository.BillingRepository;
import kr.or.kosa.cmsplusmain.domain.contract.entity.Contract;
import kr.or.kosa.cmsplusmain.domain.contract.repository.ContractCustomRepository;
import kr.or.kosa.cmsplusmain.domain.product.repository.ProductCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BillingService {

	private final BillingRepository billingRepository;
	private final BillingCustomRepository billingCustomRepository;
	private final ContractCustomRepository contractCustomRepository;

	private final ProductCustomRepository productCustomRepository;
	/*
	 * 청구 생성
	 *
	 * 총 발생 쿼리수: 4회
	 * 내용:
	 * 		존재여부 확인, 상품 이름 조회, 청구 생성, 청구상품 생성
	 * */
	@Transactional
	public void createBilling(Long vendorId, BillingCreateReq billingCreateReq) {
		Long contractId = billingCreateReq.getContractId();

		// 청구 기반 계약 존재 여부 확인
		if (!contractCustomRepository.isExistContractByUsername(contractId, vendorId)) {
			throw new EntityNotFoundException();
		}

		List<BillingProduct> billingProducts = convertToBillingProducts(billingCreateReq.getBillingProducts());

		// 청구 생성
		Billing billing = new Billing(
			Contract.of(billingCreateReq.getContractId()),
			billingCreateReq.getBillingType(),
			billingCreateReq.getBillingDate(),
			// 청구 생성시 결제일을 넣어주는데 연월일 형식으로 넣어준다.
			// 정기 청구 시 필요한 약정일은 입력된 결제일에서 일 부분만 빼서 사용
			// ex. 입력 결제일=2024.07.13 => 약정일=13
			billingCreateReq.getBillingDate().getDayOfMonth(),
			billingProducts);
		billingRepository.save(billing);
	}

	/*
	 * 청구목록 조회
	 *
	 * 총 발생 쿼리수: 3회
	 * 내용:
	 * 		청구 조회, 청구상품 목록 조회(+? batch_size=100), 전체 개수 조회
	 * */
	public PageRes<BillingListItemRes> searchBillings(Long vendorId, BillingSearchReq search, PageReq pageReq) {
		// 단일 페이지 결과
		List<BillingListItemRes> content = billingCustomRepository
			.findBillingListWithCondition(vendorId, search, pageReq)
			.stream()
			.map(BillingListItemRes::fromEntity)
			.toList();

		// 전체 개수
		int totalContentCount = billingCustomRepository.countAllBillings(vendorId, search);

		return new PageRes<>(totalContentCount, pageReq.getSize(), content);
	}

	/*
	* 청구 상세 조회
	*
	* 총 발생 쿼리수: 3회
	* 내용:
	* 	존재여부 확인, 청구목록 조회, 청구상품 목록 조회(+? batch_size=100)
	* */
	public BillingDetailRes getBillingDetail(Long vendorId, Long billingId) {
		// 고객의 청구 여부 확인
		validateBillingUser(billingId, vendorId);

		Billing billing = billingCustomRepository.findBillingDetail(billingId);
		return BillingDetailRes.fromEntity(billing);
	}

	/*
	* 청구 수정
	*
	* 총 발생 쿼리수: 6회
	* 내용:
	* 	존재여부 확인, 청구 조회, 상품 이름 조회, 청구상품 목록 조회(+? batch_size=100),
	* 	청구상품 생성(*N 청구상품 수만큼), 청구 수정,
	* 	청구상품 삭제(*N 삭제된 청구상품 수만큼)
	* */
	@Transactional
	public void updateBilling(Long vendorId, Long billingId, BillingUpdateReq billingUpdateReq) {
		// 고객의 청구 여부 확인
		validateBillingUser(billingId, vendorId);

		// 결제일, 청구서 메시지 수정
		Billing billing = billingRepository.findById(billingId).orElseThrow(IllegalStateException::new);
		billing.setBillingDate(billingUpdateReq.getBillingDate());
		billing.setInvoiceMessage(billingUpdateReq.getInvoiceMemo());

		// 신규 청구상품
		List<BillingProduct> newBillingProducts = convertToBillingProducts(billingUpdateReq.getBillingProducts());

		// 청구상품 수정
		updateBillingProducts(billing, newBillingProducts);
	}

	/*
	 * 기존 청구상품과 신규 청구상품 비교해서
	 * 새롭게 추가되거나 삭제된 것만 수정 반영
	 * */
	private void updateBillingProducts(Billing billing, List<BillingProduct> newBillingProducts) {
		// 기존 청구상품
		List<BillingProduct> oldBillingProducts = billing.getBillingProducts();

		// 새롭게 추가되는 청구상품 저장
		newBillingProducts.stream()
			.filter(nbp -> !oldBillingProducts.contains(nbp))
			.forEach(billing::addBillingProduct);

		// 없어진 청구상품 삭제
		oldBillingProducts.stream()
			.filter(obp -> !newBillingProducts.contains(obp))
			.toList()
			.forEach(billing::removeBillingProduct);
	}

	/*
	* 청구 삭제
	*
	* 총 발생 쿼리수: 4회
	* 내용:
	* 	존재여부 확인, 청구 조회, 청구상품 조회(+?), 청구 삭제(*N 청구상품수)
	* */
	@Transactional
	public void deleteBilling(Long vendorId, Long billingId) {
		// 고객의 청구 여부 확인
		validateBillingUser(billingId, vendorId);

		// 청구 상품도 동시 삭제처리된다.
		Billing billing = billingRepository.findById(billingId).orElseThrow(IllegalStateException::new);
		billing.delete();
	}

	/*
	* 청구 상품 요청 -> 청구 상품 엔티티 변환 메서드
	*
	* 요청에서 상품의 ID를 받아서
	* 상품의 ID를 토대로 상품 이름을 가져온다.
	*
	* 상품이름을 청구상품 테이블에 저장해
	* 청구상품 조회시 상품 이름만을 가져오기위한 조인을 없앤다.
	* */
	private List<BillingProduct> convertToBillingProducts(List<BillingProductReq> billingProductReqs) {
		// 상품 ID
		List<Long> productIds = billingProductReqs.stream()
			.mapToLong(BillingProductReq::getProductId)
			.boxed().toList();

		// 상품 ID -> 이름
		Map<Long, String> productIdToName = productCustomRepository.findAllProductNamesById(productIds);

		// 청구 상품 목록
		List<BillingProduct> billingProducts = billingProductReqs
			.stream()
			.map(dto -> dto.toEntity(productIdToName.get(dto.getProductId())))
			.toList();

		return billingProducts;
	}

	/*
	 * 청구 ID 존재여부
	 * 청구가 현재 로그인 고객의 회원의 청구인지 여부
	 * */
	private void validateBillingUser(Long billingId, Long vendorId) {
		if (!billingCustomRepository.isExistBillingByUsername(billingId, vendorId)) {
			throw new EntityNotFoundException("청구 ID 없음(" + billingId + ")");
		}
	}
}
