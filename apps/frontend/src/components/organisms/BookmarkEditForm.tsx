import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { updateBookmark } from '../../api/bookmarks';
import { type BookmarkInput, type BookmarkResponse } from '../../api/types';

interface BookmarkEditFormProps {
  bookmark?: BookmarkResponse; // bookmarkをオプションにする
  onSuccess: () => void;
  onCancel: () => void;
}

const BookmarkEditForm: React.FC<BookmarkEditFormProps> = ({ bookmark: propBookmark, onSuccess, onCancel }) => {
  const { id } = useParams<{ id: string }>(); // URLからIDを取得
  const bookmarkId = propBookmark?.id || (id ? parseInt(id) : null);

  const [currentBookmark, setCurrentBookmark] = useState<BookmarkResponse | null>(propBookmark || null);
  const [url, setUrl] = useState(propBookmark?.url || '');
  const [title, setTitle] = useState(propBookmark?.title || '');
  const [description, setDescription] = useState(propBookmark?.description || '');
  const [tags, setTags] = useState(propBookmark?.tags ? propBookmark.tags.join(', ') : '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [initialLoad, setInitialLoad] = useState(true); // 初回ロードフラグ

  useEffect(() => {
    if (propBookmark) {
      // 親からbookmarkが渡された場合
      setCurrentBookmark(propBookmark);
      setUrl(propBookmark.url);
      setTitle(propBookmark.title || '');
      setDescription(propBookmark.description || '');
      setTags(propBookmark.tags ? propBookmark.tags.join(', ') : '');
      setInitialLoad(false);
    } else if (bookmarkId && initialLoad) {
      // URLからIDがあり、初回ロードの場合のみフェッチ
      const fetchBookmark = async () => {
        setLoading(true);
        setError(null);
        try {
          // バックエンドが未実装のため、一時的にモックデータを使用
          const mockData: BookmarkResponse[] = [
            {
              id: 1,
              url: 'https://example.com/mock1',
              title: 'モックブックマーク 1',
              description: 'これはモックデータです。',
              tags: ['mock', 'test'],
              createdAt: '2024-05-01T10:00:00Z',
              updatedAt: '2024-05-01T10:00:00Z',
            },
            {
              id: 2,
              url: 'https://example.com/mock2',
              title: 'モックブックマーク 2',
              description: '別のモックデータです。',
              tags: ['mock', 'example'],
              createdAt: '2024-05-02T11:00:00Z',
              updatedAt: '2024-05-02T11:00:00Z',
            },
          ];
          const fetchedBookmark = mockData.find(b => b.id === bookmarkId); // モックデータから検索
          // const fetchedBookmark = await getBookmarkById(bookmarkId); // バックエンド実装後にコメント解除

          if (fetchedBookmark) {
            setCurrentBookmark(fetchedBookmark);
            setUrl(fetchedBookmark.url);
            setTitle(fetchedBookmark.title || '');
            setDescription(fetchedBookmark.description || '');
            setTags(fetchedBookmark.tags ? fetchedBookmark.tags.join(', ') : '');
          } else {
            setError('ブックマークが見つかりませんでした。');
          }
        } catch (err: unknown) {
          if (err instanceof Error) {
            setError(err.message);
          } else {
            setError('不明なエラーが発生しました。');
          }
        } finally {
          setLoading(false);
          setInitialLoad(false);
        }
      };
      fetchBookmark();
    }
  }, [propBookmark, bookmarkId, initialLoad]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    if (!bookmarkId) {
      setError('編集するブックマークのIDがありません。');
      setLoading(false);
      return;
    }

    const bookmarkData: BookmarkInput = {
      url,
      title: title || undefined,
      description: description || undefined,
      tags: tags ? tags.split(',').map(tag => tag.trim()) : [],
    };

    try {
      await updateBookmark(bookmarkId, bookmarkData);
      onSuccess();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('不明なエラーが発生しました。');
      }
    } finally {
      setLoading(false);
    }
  };

  if (loading && initialLoad) {
    return <div className="text-center py-8">読み込み中...</div>;
  }

  if (error) {
    return <div className="text-center py-8 text-red-500">エラー: {error}</div>;
  }

  if (!currentBookmark && !initialLoad) {
    return <div className="text-center py-8 text-gray-600">ブックマークが見つかりませんでした。</div>;
  }

  return (
    <div className="p-4 bg-white rounded-lg shadow-md">
      {/* <h3 className="text-xl font-bold text-gray-800 mb-4">ブックマークを編集</h3> */}
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="edit-url" className="block text-sm font-medium text-gray-700">
            URL <span className="text-red-500">*</span>
          </label>
          <input
            type="url"
            id="edit-url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          />
        </div>
        <div>
          <label htmlFor="edit-title" className="block text-sm font-medium text-gray-700">
            タイトル
          </label>
          <input
            type="text"
            id="edit-title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          />
        </div>
        <div>
          <label htmlFor="edit-description" className="block text-sm font-medium text-gray-700">
            説明
          </label>
          <textarea
            id="edit-description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          ></textarea>
        </div>
        <div>
          <label htmlFor="edit-tags" className="block text-sm font-medium text-gray-700">
            タグ (カンマ区切り)
          </label>
          <input
            type="text"
            id="edit-tags"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            placeholder="例: react, javascript, tutorial"
          />
        </div>
        {error && <p className="text-red-500 text-sm">{error}</p>}
        <div className="flex justify-end space-x-2">
          <button
            type="button"
            onClick={onCancel}
            className="py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            キャンセル
          </button>
          <button
            type="submit"
            disabled={loading}
            className="py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
          >
            {loading ? '更新中...' : '更新'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BookmarkEditForm;
