import React, { useEffect, useState } from 'react';
import { type BookmarkResponse } from '../../api/types';
import { AxiosError } from 'axios';
import DropdownMenu from '../atoms/DropdownMenu';
import { listBookmarks, deleteBookmark } from '../../api/bookmarks';

interface BookmarkListProps {
  refreshTrigger: number;
  onEditClick: (bookmark: BookmarkResponse) => void;
}

const BookmarkList: React.FC<BookmarkListProps> = ({ refreshTrigger, onEditClick }) => {
  const [bookmarks, setBookmarks] = useState<BookmarkResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // メニューアイコンを生成する関数
  const createMenuIcon = (bookmarkId: number) => (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
      strokeWidth={1.5}
      stroke="currentColor"
      className="w-5 h-5"
      aria-label={`ブックマークメニュー-${bookmarkId}`}
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M12 6.75a.75.75 0 1 1 0-1.5.75.75 0 0 1 0 1.5ZM12 12.75a.75.75 0 1 1 0-1.5.75.75 0 0 1 0 1.5ZM12 18.75a.75.75 0 1 1 0-1.5.75.75 0 0 1 0 1.5Z"
      />
    </svg>
  );

  const fetchBookmarks = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await listBookmarks();
      setBookmarks(data);
    } catch (err: unknown) {
      if (err instanceof AxiosError) { // AxiosErrorを使用
        setError(err.message);
      } else if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('不明なエラーが発生しました。');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookmarks();
  }, [refreshTrigger]);

  const handleDelete = async (id: number) => {
    if (window.confirm('このブックマークを削除してもよろしいですか？')) {
      try {
        await deleteBookmark(id);
        alert('ブックマークを削除しました。');
        // 削除成功後、ブックマークリストを再フェッチ
        fetchBookmarks();
      } catch (err: unknown) {
        let errorMessage = '不明なエラーが発生しました。';
        if (err instanceof AxiosError) { // AxiosErrorを使用
          errorMessage = err.message;
        } else if (err instanceof Error) {
          errorMessage = err.message;
        }
        alert(`エラー: ${errorMessage}`);
        setError(errorMessage); // エラー状態も更新
      }
    }
  };

  if (loading) {
    return <div className="text-center py-8">読み込み中...</div>;
  }

  return (
    <div className="container mx-auto p-4">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">ブックマーク一覧</h2>
      {error && (
        <div className="text-center py-8 text-red-500">エラー: {error}</div>
      )}
      {bookmarks.length === 0 ? (
        <p className="text-gray-600">ブックマークがありません。</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {bookmarks.map((bookmark) => (
            <div key={bookmark.id} className="bg-white p-3 rounded-lg shadow-md">
              <div className="flex justify-between items-start">
                <div className="flex-grow">
                  <h3 className="text-xl font-semibold text-blue-600 mb-1">
                    <a href={bookmark.url} target="_blank" rel="noopener noreferrer" className="hover:underline">
                      {bookmark.title || bookmark.url}
                    </a>
                  </h3>
                  {bookmark.description && (
                    <p className="text-gray-700 text-sm mb-1">{bookmark.description}</p>
                  )}
                  {bookmark.tags && bookmark.tags.length > 0 && (
                    <div className="flex flex-wrap gap-2 mb-1">
                      {bookmark.tags.map((tag: string) => (
                        <span key={tag} className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-0.5 rounded-full">
                          {tag}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
                <DropdownMenu buttonContent={createMenuIcon(bookmark.id)}>
                  <button
                    onClick={() => {
                      onEditClick(bookmark);
                      // ドロップダウンを閉じるロジックが必要な場合はここに追加
                    }}
                    className="block w-full text-left px-4 py-2.5 text-sm text-gray-700 hover:bg-orange-50 hover:text-gray-900 transition-colors duration-200"
                    role="menuitem"
                    tabIndex={-1}
                    id={`menu-item-edit-${bookmark.id}`}
                  >
                    編集
                  </button>
                  <button
                    onClick={() => {
                      handleDelete(bookmark.id);
                      // ドロップダウンを閉じるロジックが必要な場合はここに追加
                    }}
                    className="block w-full text-left px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 hover:text-red-700 transition-colors duration-200"
                    role="menuitem"
                    tabIndex={-1}
                    id={`menu-item-delete-${bookmark.id}`}
                  >
                    削除
                  </button>
                </DropdownMenu>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default BookmarkList;
