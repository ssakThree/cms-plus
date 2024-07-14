package kr.or.kosa.cmsplusmain.domain.member.service;

import jakarta.persistence.EntityNotFoundException;
import kr.or.kosa.cmsplusmain.domain.base.dto.PageReq;
import kr.or.kosa.cmsplusmain.domain.base.dto.SortPageDto;
import kr.or.kosa.cmsplusmain.domain.billing.repository.BillingCustomRepository;
import kr.or.kosa.cmsplusmain.domain.contract.dto.MemberContractListItemDto;
import kr.or.kosa.cmsplusmain.domain.contract.repository.ContractCustomRepository;
import kr.or.kosa.cmsplusmain.domain.contract.service.ContractService;
import kr.or.kosa.cmsplusmain.domain.member.dto.MemberCreateReq;
import kr.or.kosa.cmsplusmain.domain.member.dto.MemberDetail;
import kr.or.kosa.cmsplusmain.domain.member.dto.MemberListItem;
import kr.or.kosa.cmsplusmain.domain.member.entity.Member;
import kr.or.kosa.cmsplusmain.domain.member.repository.MemberCustomRepository;
import kr.or.kosa.cmsplusmain.domain.member.repository.MemberRepository;
import kr.or.kosa.cmsplusmain.domain.payment.entity.Payment;
import kr.or.kosa.cmsplusmain.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberCustomRepository memberCustomRepository;
    private final MemberRepository memberRepository;
    private final ContractCustomRepository contractCustomRepository;
    private final BillingCustomRepository billingCustomRepository;

    private final PaymentService paymentService;
    private final ContractService contractService;

    /*
     * 회원 목록 조회
     * */
    public SortPageDto.Res<MemberListItem> findMemberListItem (Long vendorId, SortPageDto.Req pageable) {

        int countMemberListItem = memberCustomRepository.countAllMemberByVendor(vendorId);

        int totalPages = (int) Math.ceil((double) countMemberListItem / pageable.getSize());

        List<MemberListItem> memberListItem = memberCustomRepository
                .findAllMemberByVendor(vendorId, pageable)
                .stream()
                .map(MemberListItem::fromEntity)
                .toList();

        return new SortPageDto.Res<>(totalPages, memberListItem);
    }

    /*
    *  회원 상세 - 기본정보
    * */
    public MemberDetail findMemberDetailById(Long vendorId, Long memberId) {

        // 회원 정보 조회
        Member member = memberCustomRepository.findMemberDetailById(vendorId, memberId)
                        .orElseThrow(() -> new EntityNotFoundException("회원 ID 없음(" + memberId + ")"));

        // 청구서 수
        int billingCount = billingCustomRepository.findBillingStandardByMemberId(vendorId, memberId);

        // 총 청구 금액
        Long totalBillingPrice = billingCustomRepository.findBillingProductByMemberId(vendorId, memberId);

        return MemberDetail.fromEntity(member, billingCount, totalBillingPrice);
    }

    /*
    * 회원 상세 - 계약리스트
    * */
    public SortPageDto.Res<MemberContractListItemDto> findContractListItemByMemberId(Long vendorId, Long memberId, PageReq pageable) {

        int countAllContractListItemByMemberId = contractCustomRepository.countContractListItemByMemberId(vendorId, memberId);
        int totalPages = (int) Math.ceil((double) countAllContractListItemByMemberId / pageable.getSize());

        List<MemberContractListItemDto> memberContractListItemDtos = contractCustomRepository
                .findContractListItemByMemberId(vendorId, memberId, pageable)
                .stream()
                .map(MemberContractListItemDto::fromEntity)
                .toList();

        return new SortPageDto.Res<>(totalPages, memberContractListItemDtos);
    }

    /*
     * 회원 등록
     * */
    @Transactional
    public void createMember(Long vendorId, MemberCreateReq memberCreateReq) {

        // 회원 정보를 DB에 저장한다.

        // 회원 존재 여부 확인 ( 휴대전화 번호 기준 )
        if(memberCustomRepository.idExistMemberByPhone(memberCreateReq.getMemberPhone())){
            log.info("회원이 이미 존재합니다: " + memberCreateReq.getMemberPhone());
        }
        else {
            Member member = memberCreateReq.toEntity(vendorId);
            memberRepository.save(member);
        }

        // 결제 정보를 DB에 저장한다.
        Payment payment = paymentService.createPayment(memberCreateReq.getPaymentCreateReq());

        // 계약 정보를 DB에 저장한다.
//        contractService.createContract(vendorId, member, payment, memberCreateReq.getContractCreateReq());

    }
}
