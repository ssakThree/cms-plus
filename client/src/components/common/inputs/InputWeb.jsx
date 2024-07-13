import { useRef, useState } from 'react';

const InputWeb = ({
  id,
  label,
  required,
  disabled,
  type = 'text',
  placeholder,
  classContainer = '',
  classLabel = '',
  classInput = '',
  ...props
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const inputRef = useRef(null);

  // 비밀번호 표시 여부
  const handleTogglePassword = () => {
    setShowPassword(!showPassword);
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };

  return (
    <div className={`${classContainer}`}>
      <label
        className={`${classLabel} block text-text_black text-15 font-700 mb-2 ml-2 
                    ${required ? "after:ml-1 after:text-red-500 after:content-['*']" : ''}`}
        htmlFor={id}>
        {label}
      </label>
      <div className='relative '>
        <input
          ref={inputRef}
          className={`${classInput} placeholder:text-text_grey text-black border
                    ${disabled && 'bg-ipt_disa '} border-ipt_border focus:border-mint focus:outline-none 
                    focus:ring-mint focus:ring-1 placeholder:text-xs text-sm p-4  rounded-lg w-full`}
          id={id}
          disabled={disabled}
          type={type !== 'password' ? type : showPassword ? 'text' : 'password'}
          placeholder={placeholder}
          {...props}
        />
        {type === 'password' && (
          <img
            src={showPassword ? '/src/assets/openeye.svg' : '/src/assets/closeeye.svg'}
            alt='Toggle password visibility'
            className='absolute top-1/2 transform -translate-y-1/2 right-3 cursor-pointer w-6'
            onClick={handleTogglePassword}
          />
        )}
      </div>
    </div>
  );
};

export default InputWeb;
