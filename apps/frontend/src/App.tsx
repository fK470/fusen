import { useState } from 'react';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import BookmarkList from './components/organisms/BookmarkList';
import BookmarkForm from './components/organisms/BookmarkForm';
import BookmarkEditForm from './components/organisms/BookmarkEditForm';
import Modal from './components/atoms/Modal';
import useMediaQuery from './hooks/useMediaQuery';
import { type BookmarkResponse } from './api/types';

function App() {
  const [refreshList, setRefreshList] = useState(0);
  const [isNewModalOpen, setIsNewModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingBookmark, setEditingBookmark] = useState<BookmarkResponse | null>(null);
  const isDesktop = useMediaQuery('(min-width: 768px)'); // 768px以上をデスクトップと判定
  const navigate = useNavigate();
  const location = useLocation();

  const handleBookmarkAdded = () => {
    setRefreshList(prev => prev + 1);
    if (isDesktop) {
      setIsNewModalOpen(false);
    } else {
      navigate('/');
    }
  };

  const handleBookmarkUpdated = () => {
    setRefreshList(prev => prev + 1);
    if (isDesktop) {
      setIsEditModalOpen(false);
      setEditingBookmark(null);
    } else {
      navigate('/');
    }
  };

  const handleNewClick = () => {
    if (isDesktop) {
      setIsNewModalOpen(true);
    } else {
      navigate('/new');
    }
  };

  const handleEditClick = (bookmark: BookmarkResponse) => {
    setEditingBookmark(bookmark);
    if (isDesktop) {
      setIsEditModalOpen(true);
    } else {
      navigate(`/edit/${bookmark.id}`);
    }
  };

  const handleModalClose = () => {
    setIsNewModalOpen(false);
    setIsEditModalOpen(false);
    setEditingBookmark(null);
    // モーダルを閉じたときにURLをルートに戻す（モバイルで直接/newや/editにアクセスした場合の考慮）
    if (location.pathname !== '/') {
      navigate('/');
    }
  };

  return (
    <div className="min-h-screen bg-light-orange">
      <header className="bg-orange-200 text-gray-800 p-4 shadow-md">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-3xl font-bold">Fusen App</h1>
          <button
            onClick={handleNewClick}
            className="bg-white text-orange-600 px-4 py-2 rounded-md shadow hover:bg-orange-100"
          >
            新規作成
          </button>
        </div>
      </header>
      <main className="py-8">
        <Routes>
          <Route
            path="/"
            element={<BookmarkList refreshTrigger={refreshList} onEditClick={handleEditClick} />}
          />
          {/* モバイル用のルーティングはisDesktopがfalseの場合のみ有効 */}
          {!isDesktop && (
            <>
              <Route
                path="/new"
                element={<BookmarkForm onSuccess={handleBookmarkAdded} onCancel={() => navigate('/')} />}
              />
              <Route
                path="/edit/:id"
                element={<BookmarkEditForm onSuccess={handleBookmarkUpdated} onCancel={() => navigate('/')} />}
              />
            </>
          )}
        </Routes>

        {/* 新規作成モーダル (PCのみ) */}
        {isDesktop && (
          <Modal isOpen={isNewModalOpen} onClose={handleModalClose} title="新しいブックマークを追加">
            <BookmarkForm onSuccess={handleBookmarkAdded} onCancel={handleModalClose} />
          </Modal>
        )}

        {/* 編集モーダル (PCのみ) */}
        {isDesktop && editingBookmark && ( // editingBookmarkが存在する場合のみレンダリング
          <Modal isOpen={isEditModalOpen} onClose={handleModalClose} title="ブックマークを編集">
            <BookmarkEditForm
              bookmark={editingBookmark} // editingBookmarkはnullではないことが保証される
              onSuccess={handleBookmarkUpdated}
              onCancel={handleModalClose}
            />
          </Modal>
        )}
      </main>
    </div>
  );
}

export default App;
