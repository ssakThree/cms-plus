import { useState } from 'react';
import Input from '@/components/common/inputs/Input';
import { useInvoiceStore } from '@/stores/useInvoiceStore';
import { verifyCMS } from '@/apis/validation';

const AccountInfo = ({ accountInfo, setAccountInfo }) => {
  const selectedCard = useInvoiceStore(state => state.selectedCard);

  const [isVerifying, setIsVerifying] = useState(false);
  const [isVerified, setIsVerified] = useState(false);
  const [verificationResult, setVerificationResult] = useState(null);

  const formatBirthDate = value => {
    const cleaned = value.replace(/\D/g, '');
    let formatted = cleaned;

    if (cleaned.length > 4) {
      formatted = `${cleaned.slice(0, 4)}-${cleaned.slice(4)}`;
    }
    if (cleaned.length > 6) {
      formatted = `${formatted.slice(0, 7)}-${formatted.slice(7)}`;
    }

    return formatted.slice(0, 10);
  };

  const handleInputChange = e => {
    const { name, value } = e.target;
    let formattedValue = value;

    if (name == 'accountOwnerBirth') {
      formattedValue = formatBirthDate(value);
    }

    setAccountInfo(prevState => ({
      ...prevState,
      [name]: formattedValue,
    }));
  };

  const handleAccountVerification = async e => {
    e.preventDefault();
    if (isVerified) return;

    setIsVerifying(true);
    try {
      const accountData = {
        paymentMethod: 'CMS',
        accountNumber: accountInfo.accountNumber,
        accountOwner: accountInfo.accountOwner,
        accountOwnerBirth: accountInfo.accountOwnerBirth,
      };

      const result = await verifyCMS(accountData);
      setVerificationResult(result);
      setIsVerified(true);
      console.log('Account verification result:', result);
    } catch (error) {
      console.error('Account verification failed:', error);
      setVerificationResult({ error: '계좌 인증에 실패했습니다.' });
      setIsVerified(false);
    } finally {
      setIsVerifying(false);
    }
  };

  return (
    <div>
      <h3 className='mb-8 text-base font-semibold text-gray-700'>
        회원님의
        <br />
        결제계좌 정보를 입력해주세요.
      </h3>

      <div className='flex flex-col bg-white p-1'>
        <form className='space-y-4' onSubmit={handleAccountVerification}>
          <Input
            label='은행'
            name='bankNumber'
            type='text'
            required
            placeholder={selectedCard}
            disabled
            class='mt-1 block w-full px-3 py-2 bg-white border border-slate-300 rounded-md text-sm shadow-sm placeholder-slate-400
          focus:outline-none disabled:bg-slate-50 disabled:text-slate-500 disabled:border-slate-200 disabled:shadow-none'
          />
          <Input
            label='예금주명'
            name='accountOwner'
            type='text'
            required
            placeholder='최대 20자리'
            value={accountInfo.accountOwner}
            onChange={handleInputChange}
            maxLength={20}
          />
          <Input
            label='계좌번호'
            name='accountNumber'
            type='text'
            required
            placeholder='최대 20자리'
            value={accountInfo.accountNumber}
            onChange={handleInputChange}
            maxLength={20}
          />
          <Input
            label='생년월일'
            name='accountOwnerBirth'
            type='text'
            required
            placeholder='YYYY-MM-DD'
            value={accountInfo.accountOwnerBirth}
            onChange={handleInputChange}
            maxLength={10}
          />
          <button
            type='submit'
            className={`mt-4 w-full rounded-lg border py-2 text-sm font-normal transition-colors ${
              isVerified
                ? 'border-green-400 bg-green-50 text-green-400'
                : 'border-teal-400 bg-white text-teal-400 hover:bg-teal-50'
            }`}
            disabled={isVerifying || isVerified}>
            {isVerified ? '인증 완료' : isVerifying ? '인증 중...' : '계좌 인증하기'}
          </button>
        </form>
        {verificationResult && !isVerified && (
          <div className='mt-4 p-2 text-sm rounded bg-red-100 text-red-700'>
            {verificationResult.error}
          </div>
        )}
      </div>
    </div>
  );
};

export default AccountInfo;
