import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import BookmarkEditForm from '../BookmarkEditForm';
import * as bookmarkApi from '../../../api/bookmarks';
import { type BookmarkResponse } from '../../../api/types';

// bookmarkApiモジュール全体をモック
vi.mock('../../../api/bookmarks', () => ({
  updateBookmark: vi.fn(),
}));

describe('BookmarkEditForm', () => {
  const mockBookmark: BookmarkResponse = {
    id: 1,
    url: 'https://example.com/edit',
    title: 'Original Title',
    description: 'Original Description',
    tags: ['tagA', 'tagB'],
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    // window.alertをモック
    vi.spyOn(window, 'alert').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('フォームが既存のブックマークデータで正常にレンダリングされること', () => {
    render(<BookmarkEditForm bookmark={mockBookmark} onSuccess={vi.fn()} onCancel={vi.fn()} />);

    expect(screen.getByLabelText(/URL/i)).toHaveValue(mockBookmark.url);
    expect(screen.getByLabelText(/タイトル/i)).toHaveValue(mockBookmark.title);
    expect(screen.getByLabelText(/説明/i)).toHaveValue(mockBookmark.description);
    expect(screen.getByLabelText(/タグ \(カンマ区切り\)/i)).toHaveValue(mockBookmark.tags.join(', '));
    expect(screen.getByRole('button', { name: '更新' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'キャンセル' })).toBeInTheDocument();
  });

  it('フォームの入力と更新が正常に行われること', async () => {
    const mockOnSuccess = vi.fn();
    const mockOnCancel = vi.fn();
    vi.mocked(bookmarkApi.updateBookmark).mockResolvedValue({
      ...mockBookmark,
      title: 'Updated Title',
      description: 'Updated Description',
    });

    render(<BookmarkEditForm bookmark={mockBookmark} onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    fireEvent.change(screen.getByLabelText(/タイトル/i), { target: { value: 'Updated Title' } });
    fireEvent.change(screen.getByLabelText(/説明/i), { target: { value: 'Updated Description' } });

    fireEvent.click(screen.getByRole('button', { name: '更新' }));

    await waitFor(() => {
      expect(bookmarkApi.updateBookmark).toHaveBeenCalledTimes(1);
      expect(bookmarkApi.updateBookmark).toHaveBeenCalledWith(mockBookmark.id, {
        url: mockBookmark.url,
        title: 'Updated Title',
        description: 'Updated Description',
        tags: mockBookmark.tags,
      });
    });

    expect(mockOnSuccess).toHaveBeenCalledTimes(1);
  });

  it('URLが空の場合にAPIが呼び出されないこと', async () => {
    render(<BookmarkEditForm bookmark={mockBookmark} onSuccess={vi.fn()} onCancel={vi.fn()} />);

    fireEvent.change(screen.getByLabelText(/URL/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: '更新' }));

    // HTML5バリデーションによりフォーム送信がブロックされるため、APIは呼び出されない
    await waitFor(() => {
      expect(bookmarkApi.updateBookmark).not.toHaveBeenCalled();
    });
  });

  it('APIエラーが発生した場合にエラーメッセージが表示されること', async () => {
    const errorMessage = 'ブックマークの更新に失敗しました。';
    vi.mocked(bookmarkApi.updateBookmark).mockRejectedValue(new Error(errorMessage));

    render(<BookmarkEditForm bookmark={mockBookmark} onSuccess={vi.fn()} onCancel={vi.fn()} />);

    fireEvent.change(screen.getByLabelText(/URL/i), { target: { value: 'https://example.com/edit' } });
    fireEvent.change(screen.getByLabelText(/タイトル/i), { target: { value: 'Updated Title' } });
    fireEvent.click(screen.getByRole('button', { name: '更新' }));

    await waitFor(() => {
      expect(screen.getByText(/ブックマークの更新に失敗しました。/)).toBeInTheDocument();
    });
    expect(bookmarkApi.updateBookmark).toHaveBeenCalledTimes(1);
  });

  it('キャンセルボタンをクリックするとonCancelが呼び出されること', () => {
    const mockOnSuccess = vi.fn();
    const mockOnCancel = vi.fn();

    render(<BookmarkEditForm bookmark={mockBookmark} onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    fireEvent.click(screen.getByRole('button', { name: 'キャンセル' }));

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
    expect(mockOnSuccess).not.toHaveBeenCalled(); // onSuccessは呼び出されないことを確認
  });
});
