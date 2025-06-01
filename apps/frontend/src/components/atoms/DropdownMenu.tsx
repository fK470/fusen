import React, { useState, useRef, useEffect } from 'react';

interface DropdownMenuProps {
  children: React.ReactNode;
  buttonContent: React.ReactNode;
}

const DropdownMenu: React.FC<DropdownMenuProps> = ({ children, buttonContent }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [menuAlignment, setMenuAlignment] = useState<'left' | 'right'>('right');
  const menuRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);

  const toggleMenu = (event: React.MouseEvent) => {
    event.stopPropagation(); // イベントの伝播を停止
    setIsOpen((prev) => !prev);
  };

  const handleClickOutside = (event: MouseEvent) => {
    if (
      menuRef.current &&
      !menuRef.current.contains(event.target as Node) &&
      buttonRef.current &&
      !buttonRef.current.contains(event.target as Node)
    ) {
      setIsOpen(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      document.addEventListener('mouseup', handleClickOutside);
    } else {
      document.removeEventListener('mouseup', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mouseup', handleClickOutside);
    };
  }, [isOpen]);

  useEffect(() => {
    if (isOpen && buttonRef.current) {
      const buttonRect = buttonRef.current.getBoundingClientRect();
      const viewportWidth = window.innerWidth;
      // メニューの推定最大幅 (max-w-xs = 320px)
      const estimatedMenuWidth = 320;

      if (buttonRect.right + estimatedMenuWidth > viewportWidth) {
        // ボタンの右側にメニューを表示するスペースがない場合、左寄せにする
        setMenuAlignment('left');
      } else {
        setMenuAlignment('right');
      }
    }
  }, [isOpen]);

  const menuPositionClass = menuAlignment === 'left' ? 'right-0' : 'left-0';
  const originClass = menuAlignment === 'left' ? 'origin-top-left' : 'origin-top-right';

  return (
    <div className="relative inline-block text-left">
      <div>
        <button
          ref={buttonRef}
          type="button"
          className="inline-flex items-center justify-center p-1.5 rounded-md text-gray-600 hover:bg-gray-200 hover:text-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 shadow-sm"
          id="menu-button"
          aria-expanded="true"
          aria-haspopup="true"
          onClick={toggleMenu}
          aria-label="ブックマークメニュー"
        >
          {buttonContent}
        </button>
      </div>

      {isOpen && (
        <div
          ref={menuRef}
          className={`${originClass} absolute ${menuPositionClass} mt-2 w-auto min-w-max rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 focus:outline-none z-10`}
          role="menu"
          aria-orientation="vertical"
          aria-labelledby="menu-button"
          tabIndex={-1}
        >
          <div className="py-1" role="none">
            {children}
          </div>
        </div>
      )}
    </div>
  );
};

export default DropdownMenu;
