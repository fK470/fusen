import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import BookmarkList from '../BookmarkList';
import * as bookmarkApi from '../../../api/bookmarks';
import { type BookmarkResponse } from '../../../api/types';

// bookmarkApiモジュール全体をモック
vi.mock('../../../api/bookmarks', () => ({
  listBookmarks: vi.fn(),
  createBookmark: vi.fn(),
  getBookmarkById: vi.fn(),
  updateBookmark: vi.fn(),
  deleteBookmark: vi.fn(),
}));

describe('BookmarkList', () => {
  beforeEach(() => {
    // 各テストの前にモックの呼び出し回数をリセット
    vi.clearAllMocks();
  });

  it('ブックマークが正常に表示されること', async () => {
    const mockBookmarks: BookmarkResponse[] = [
      {
        id: 1,
        url: 'https://example.com/1',
        title: 'Test Bookmark 1',
        description: 'Description for test 1',
        tags: ['tag1', 'tag2'],
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
      {
        id: 2,
        url: 'https://example.com/2',
        title: 'Test Bookmark 2',
        description: 'Description for test 2',
        tags: ['tag3'],
        createdAt: '2024-01-02T00:00:00Z',
        updatedAt: '2024-01-02T00:00:00Z',
      },
    ];

    // listBookmarksがモックデータを返すように設定
    vi.mocked(bookmarkApi.listBookmarks).mockResolvedValue(mockBookmarks);

    render(<BookmarkList refreshTrigger={0} onEditClick={vi.fn()} />);

    // ローディング状態が表示されることを確認
    expect(screen.getByText('読み込み中...')).toBeInTheDocument();

    // データがロードされるのを待つ
    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });

    // ブックマークのタイトルが表示されることを確認
    expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();
    expect(screen.getByText('Test Bookmark 2')).toBeInTheDocument();

    // ブックマークの説明が表示されることを確認
    expect(screen.getByText('Description for test 1')).toBeInTheDocument();
    expect(screen.getByText('Description for test 2')).toBeInTheDocument();

    // タグが表示されることを確認
    expect(screen.getByText('tag1')).toBeInTheDocument();
    expect(screen.getByText('tag2')).toBeInTheDocument();
    expect(screen.getByText('tag3')).toBeInTheDocument();

    // listBookmarksが呼び出されたことを確認
    expect(bookmarkApi.listBookmarks).toHaveBeenCalledTimes(1);
  });

  it('エラーメッセージが正常に表示されること', async () => {
    const errorMessage = 'APIエラーが発生しました。';
    vi.mocked(bookmarkApi.listBookmarks).mockRejectedValue(new Error(errorMessage));

    render(<BookmarkList refreshTrigger={0} onEditClick={vi.fn()} />);

    // ローディング状態が表示されることを確認
    expect(screen.getByText('読み込み中...')).toBeInTheDocument();

    // エラーメッセージが表示されるのを待つ
    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      expect(screen.getByText(`エラー: ${errorMessage}`)).toBeInTheDocument();
    });

    // listBookmarksが呼び出されたことを確認
    expect(bookmarkApi.listBookmarks).toHaveBeenCalledTimes(1);
  });

  it('ブックマークがない場合にメッセージが表示されること', async () => {
    vi.mocked(bookmarkApi.listBookmarks).mockResolvedValue([]);

    render(<BookmarkList refreshTrigger={0} onEditClick={vi.fn()} />);

    // ローディング状態が表示されることを確認
    expect(screen.getByText('読み込み中...')).toBeInTheDocument();

    // データがロードされるのを待つ
    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });

    // ブックマークがないメッセージが表示されることを確認
    expect(screen.getByText('ブックマークがありません。')).toBeInTheDocument();

    // listBookmarksが呼び出されたことを確認
    expect(bookmarkApi.listBookmarks).toHaveBeenCalledTimes(1);
  });

  it('メニューボタンが表示され、クリックするとドロップダウンメニューが表示されること', async () => {
    const mockBookmarks: BookmarkResponse[] = [
      {
        id: 1,
        url: 'https://example.com/1',
        title: 'Test Bookmark 1',
        description: 'Description for test 1',
        tags: ['tag1', 'tag2'],
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
    ];
    vi.mocked(bookmarkApi.listBookmarks).mockResolvedValue(mockBookmarks);

    render(<BookmarkList refreshTrigger={0} onEditClick={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();
    });

    // メニューボタン（aria-labelで識別）が表示されていることを確認
    const menuButton = screen.getByLabelText(/ブックマークメニュー-\d+/);
    expect(menuButton).toBeInTheDocument();

    // ドロップダウンメニューが最初は表示されていないことを確認
    expect(screen.queryByRole('menu')).not.toBeInTheDocument();

    // メニューボタンをクリック
    fireEvent.click(menuButton);

    // ドロップダウンメニューが表示されることを確認
    await waitFor(() => {
      expect(screen.getByRole('menu')).toBeInTheDocument();
    });

    // メニュー項目が表示されることを確認
    expect(screen.getByText('編集')).toBeInTheDocument();
    expect(screen.getByText('削除')).toBeInTheDocument();
  });

  it('編集メニューをクリックするとonEditClickが呼び出されること', async () => {
    const mockBookmarks: BookmarkResponse[] = [
      {
        id: 1,
        url: 'https://example.com/1',
        title: 'Test Bookmark 1',
        description: 'Description for test 1',
        tags: ['tag1', 'tag2'],
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
    ];
    vi.mocked(bookmarkApi.listBookmarks).mockResolvedValue(mockBookmarks);
    const mockOnEditClick = vi.fn();

    render(<BookmarkList refreshTrigger={0} onEditClick={mockOnEditClick} />);

    await waitFor(() => {
      expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();
    });

    const menuButton = screen.getByLabelText(/ブックマークメニュー-\d+/);
    fireEvent.click(menuButton);

    await waitFor(() => {
      expect(screen.getByRole('menu')).toBeInTheDocument();
    });

    const editButton = screen.getByText('編集');
    fireEvent.click(editButton);

    // onEditClickが正しいブックマークオブジェクトで呼び出されたことを確認
    expect(mockOnEditClick).toHaveBeenCalledTimes(1);
    expect(mockOnEditClick).toHaveBeenCalledWith(mockBookmarks[0]);
  });

  it('削除メニューをクリックするとhandleDeleteが呼び出され、ブックマークが削除されること', async () => {
    const mockBookmarks: BookmarkResponse[] = [
      {
        id: 1,
        url: 'https://example.com/1',
        title: 'Test Bookmark 1',
        description: 'Description for test 1',
        tags: ['tag1', 'tag2'],
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
    ];
    vi.mocked(bookmarkApi.listBookmarks)
      .mockResolvedValueOnce(mockBookmarks) // 最初のlistBookmarks呼び出し
      .mockResolvedValueOnce([]); // 削除後のlistBookmarks呼び出し
    // window.confirmをモック
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    // window.alertをモック
    vi.spyOn(window, 'alert').mockImplementation(() => {});
    // deleteBookmarkが成功することをモック
    vi.mocked(bookmarkApi.deleteBookmark).mockResolvedValue(undefined);

    render(<BookmarkList refreshTrigger={0} onEditClick={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();
    });

    const menuButton = screen.getByLabelText(/ブックマークメニュー-\d+/);
    fireEvent.click(menuButton);

    await waitFor(() => {
      expect(screen.getByRole('menu')).toBeInTheDocument();
    });

    const deleteButton = screen.getByText('削除');
    fireEvent.click(deleteButton);

    // confirmが呼び出されたことを確認
    expect(window.confirm).toHaveBeenCalledTimes(1);
    expect(window.confirm).toHaveBeenCalledWith('このブックマークを削除してもよろしいですか？');

    // ブックマークが画面から消えるのを待つ
    await waitFor(() => {
      expect(screen.queryByText('Test Bookmark 1')).not.toBeInTheDocument();
    });

    // deleteBookmarkが呼び出されたことを確認
    expect(bookmarkApi.deleteBookmark).toHaveBeenCalledTimes(1);
    expect(bookmarkApi.deleteBookmark).toHaveBeenCalledWith(1);

    // alertが呼び出されたことを確認
    expect(window.alert).toHaveBeenCalledTimes(1);
    expect(window.alert).toHaveBeenCalledWith('ブックマークを削除しました。');
  });

  it('削除メニューをクリックし、確認ダイアログでキャンセルするとブックマークが削除されないこと', async () => {
    const mockBookmarks: BookmarkResponse[] = [
      {
        id: 1,
        url: 'https://example.com/1',
        title: 'Test Bookmark 1',
        description: 'Description for test 1',
        tags: ['tag1', 'tag2'],
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
    ];
    vi.mocked(bookmarkApi.listBookmarks).mockResolvedValue(mockBookmarks);
    // window.confirmをモックし、falseを返すように設定
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    // window.alertをモック
    vi.spyOn(window, 'alert').mockImplementation(() => {});
    // deleteBookmarkが呼び出されないことを確認するためにモック
    vi.mocked(bookmarkApi.deleteBookmark).mockResolvedValue(undefined);

    render(<BookmarkList refreshTrigger={0} onEditClick={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();
    });

    const menuButton = screen.getByLabelText(/ブックマークメニュー-\d+/);
    fireEvent.click(menuButton);

    await waitFor(() => {
      expect(screen.getByRole('menu')).toBeInTheDocument();
    });

    const deleteButton = screen.getByText('削除');
    fireEvent.click(deleteButton);

    // confirmが呼び出されたことを確認
    expect(window.confirm).toHaveBeenCalledTimes(1);
    expect(window.confirm).toHaveBeenCalledWith('このブックマークを削除してもよろしいですか？');

    // deleteBookmarkが呼び出されていないことを確認
    expect(bookmarkApi.deleteBookmark).not.toHaveBeenCalled();

    // ブックマークが画面に残っていることを確認
    expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();

    // alertが呼び出されていないことを確認
    expect(window.alert).not.toHaveBeenCalled();
  });

  it('削除API呼び出しが失敗した場合にエラーメッセージが表示されること', async () => {
    const mockBookmarks: BookmarkResponse[] = [
      {
        id: 1,
        url: 'https://example.com/1',
        title: 'Test Bookmark 1',
        description: 'Description for test 1',
        tags: ['tag1', 'tag2'],
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
    ];
    const errorMessage = 'ブックマークの削除に失敗しました。';
    vi.mocked(bookmarkApi.listBookmarks).mockResolvedValue(mockBookmarks);
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    vi.spyOn(window, 'alert').mockImplementation(() => {});
    vi.mocked(bookmarkApi.deleteBookmark).mockRejectedValue(new Error(errorMessage));

    render(<BookmarkList refreshTrigger={0} onEditClick={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();
    });

    const menuButton = screen.getByLabelText(/ブックマークメニュー-\d+/);
    fireEvent.click(menuButton);

    await waitFor(() => {
      expect(screen.getByRole('menu')).toBeInTheDocument();
    });

    const deleteButton = screen.getByText('削除');
    fireEvent.click(deleteButton);

    // confirmが呼び出されたことを確認
    expect(window.confirm).toHaveBeenCalledTimes(1);
    expect(window.confirm).toHaveBeenCalledWith('このブックマークを削除してもよろしいですか？');

    // deleteBookmarkが呼び出されたことを確認
    expect(bookmarkApi.deleteBookmark).toHaveBeenCalledTimes(1);
    expect(bookmarkApi.deleteBookmark).toHaveBeenCalledWith(1);

    // エラーメッセージが表示されることを確認
    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledTimes(1);
      expect(window.alert).toHaveBeenCalledWith(`エラー: ${errorMessage}`);
    });

    // ブックマークが画面に残っていることを確認（削除失敗のため）
    expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();
  });

  it('refreshTriggerが変更されたときにブックマークが再フェッチされること', async () => {
    const mockBookmarks1: BookmarkResponse[] = [
      {
        id: 1,
        url: 'https://example.com/1',
        title: 'Test Bookmark 1',
        description: 'Description for test 1',
        tags: ['tag1'],
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
    ];
    const mockBookmarks2: BookmarkResponse[] = [
      {
        id: 3,
        url: 'https://example.com/3',
        title: 'Test Bookmark 3',
        description: 'Description for test 3',
        tags: ['tag4'],
        createdAt: '2024-01-03T00:00:00Z',
        updatedAt: '2024-01-03T00:00:00Z',
      },
    ];

    vi.mocked(bookmarkApi.listBookmarks)
      .mockResolvedValueOnce(mockBookmarks1) // 最初の呼び出し
      .mockResolvedValueOnce(mockBookmarks2); // 2回目の呼び出し

    const { rerender } = render(<BookmarkList refreshTrigger={0} onEditClick={vi.fn()} />);

    // 最初のフェッチが完了し、ローディングが消えるのを待つ
    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });
    // ブックマーク1が表示されることを確認
    expect(screen.getByText('Test Bookmark 1')).toBeInTheDocument();
    expect(bookmarkApi.listBookmarks).toHaveBeenCalledTimes(1);

    // refreshTriggerを変更して再レンダリング
    rerender(<BookmarkList refreshTrigger={1} onEditClick={vi.fn()} />);

    // 2回目のフェッチが完了し、ローディングが消えるのを待つ
    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });
    // ブックマーク1が消え、ブックマーク3が表示されることを確認
    expect(screen.queryByText('Test Bookmark 1')).not.toBeInTheDocument();
    expect(screen.getByText('Test Bookmark 3')).toBeInTheDocument();
    expect(bookmarkApi.listBookmarks).toHaveBeenCalledTimes(2);
  });
});
