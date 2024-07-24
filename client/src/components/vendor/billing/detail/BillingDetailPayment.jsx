import InputWeb from '@/components/common/inputs/InputWeb';
import { formatId } from '@/utils/format/formatId';

const BillingDetailPayment = ({ contractId, paymentType, paymentMethod }) => {
  return (
    <>
      <p className='text-text_black text-xl font-800'>결제정보</p>
      <div className='grid grid-cols-4 gap-6 my-8'>
        <InputWeb
          id='contractId'
          label='계약번호'
          value={formatId(contractId)}
          type='text'
          disabled={true}
        />
        <InputWeb
          id='paymentType'
          label='결제방식'
          value={paymentType.title}
          type='text'
          disabled={true}
        />
        <InputWeb
          id='paymentMethod'
          label='결제수단'
          value={paymentMethod ? paymentMethod.title : '-'}
          type='text'
          disabled={true}
        />
      </div>
    </>
  );
};

export default BillingDetailPayment;
