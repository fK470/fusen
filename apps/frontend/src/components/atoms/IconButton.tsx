import React from 'react';

interface IconButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  icon: React.ReactNode;
  label: string; // アクセシビリティのためのラベル
}

const IconButton: React.FC<IconButtonProps> = ({ icon, label, className = '', ...props }) => {
  return (
    <button
      type="button"
      className={`inline-flex items-center justify-center p-1.5 rounded-md text-gray-600 hover:bg-gray-200 hover:text-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 shadow-sm ${className}`}
      aria-label={label}
      {...props}
    >
      {icon}
    </button>
  );
};

export default IconButton;
