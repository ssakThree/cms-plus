const Card = props => (
  <svg width='16' height='16' viewBox='0 0 16 16' xmlns='http://www.w3.org/2000/svg' {...props}>
    <g clipPath='url(#clip0_487_3802)'>
      <path
        d='M0.992188 11.5151C0.992188 11.9503 1.16504 12.3676 1.47272 12.6752C1.78039 12.9829 2.19769 13.1558 2.63281 13.1558H12.4766C12.9117 13.1558 13.329 12.9829 13.6367 12.6752C13.9443 12.3676 14.1172 11.9503 14.1172 11.5151V7.00342H0.992188V11.5151ZM2.92578 9.28857C2.92578 9.05547 3.01838 8.83192 3.18321 8.66709C3.34803 8.50227 3.57159 8.40967 3.80469 8.40967H5.21094C5.44404 8.40967 5.66759 8.50227 5.83242 8.66709C5.99724 8.83192 6.08984 9.05547 6.08984 9.28857V9.87451C6.08984 10.1076 5.99724 10.3312 5.83242 10.496C5.66759 10.6608 5.44404 10.7534 5.21094 10.7534H3.80469C3.57159 10.7534 3.34803 10.6608 3.18321 10.496C3.01838 10.3312 2.92578 10.1076 2.92578 9.87451V9.28857Z'
        fill={props.color}
      />
      <path
        d='M12.4766 2.84277H2.63281C2.19769 2.84277 1.78039 3.01562 1.47272 3.3233C1.16504 3.63098 0.992188 4.04828 0.992188 4.4834V5.24512H14.1172V4.4834C14.1172 4.04828 13.9443 3.63098 13.6367 3.3233C13.329 3.01562 12.9117 2.84277 12.4766 2.84277Z'
        fill={props.color}
      />
    </g>
    <defs>
      <clipPath id='clip0_487_3802'>
        <rect width='15' height='15' fill='white' transform='translate(0.0546875 0.5)' />
      </clipPath>
    </defs>
  </svg>
);

export default Card;
