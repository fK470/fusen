import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import BookmarkForm from '../BookmarkForm';
import * as bookmarkApi from '../../../api/bookmarks';

// bookmarkApiモジュール全体をモック
vi.mock('../../../api/bookmarks', () => ({
  createBookmark: vi.fn(),
}));

describe('BookmarkForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // window.alertをモック
    vi.spyOn(window, 'alert').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('フォームが正常にレンダリングされること', () => {
    render(<BookmarkForm onSuccess={vi.fn()} onCancel={vi.fn()} />);

    expect(screen.getByLabelText(/URL/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/タイトル/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/説明/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/タグ \(カンマ区切り\)/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'ブックマークを追加' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'キャンセル' })).toBeInTheDocument();
  });

  it('フォームの入力と送信が正常に行われること', async () => {
    const mockOnSuccess = vi.fn();
    const mockOnCancel = vi.fn();
    vi.mocked(bookmarkApi.createBookmark).mockResolvedValue({
      id: 1,
      url: 'https://example.com',
      title: 'Test Title',
      description: 'Test Description',
      tags: ['test', 'mock'],
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
    });

    render(<BookmarkForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    fireEvent.change(screen.getByLabelText(/URL/i), { target: { value: 'https://example.com' } });
    fireEvent.change(screen.getByLabelText(/タイトル/i), { target: { value: 'Test Title' } });
    fireEvent.change(screen.getByLabelText(/説明/i), { target: { value: 'Test Description' } });
    fireEvent.change(screen.getByLabelText(/タグ \(カンマ区切り\)/i), { target: { value: 'test,mock' } });

    fireEvent.click(screen.getByRole('button', { name: 'ブックマークを追加' }));

    await waitFor(() => {
      expect(bookmarkApi.createBookmark).toHaveBeenCalledTimes(1);
      expect(bookmarkApi.createBookmark).toHaveBeenCalledWith({
        url: 'https://example.com',
        title: 'Test Title',
        description: 'Test Description',
        tags: ['test', 'mock'],
      });
    });

    expect(mockOnSuccess).toHaveBeenCalledTimes(1);
    // フォームがリセットされたことを確認
    expect(screen.getByLabelText(/URL/i)).toHaveValue('');
    expect(screen.getByLabelText(/タイトル/i)).toHaveValue('');
    expect(screen.getByLabelText(/説明/i)).toHaveValue('');
    expect(screen.getByLabelText(/タグ \(カンマ区切り\)/i)).toHaveValue('');
  });

  it('URLが空の場合にAPIが呼び出されないこと', async () => {
    render(<BookmarkForm onSuccess={vi.fn()} onCancel={vi.fn()} />);

    fireEvent.change(screen.getByLabelText(/タイトル/i), { target: { value: 'Test Title' } });
    fireEvent.click(screen.getByRole('button', { name: 'ブックマークを追加' }));

    // HTML5バリデーションによりフォーム送信がブロックされるため、APIは呼び出されない
    await waitFor(() => {
      expect(bookmarkApi.createBookmark).not.toHaveBeenCalled();
    });
  });

  it('タイトルが空の場合にAPIが呼び出されること (タイトルは必須ではないため)', async () => {
    const mockOnSuccess = vi.fn();
    vi.mocked(bookmarkApi.createBookmark).mockResolvedValue({
      id: 1,
      url: 'https://example.com',
      title: null,
      description: null,
      tags: [],
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
    });

    render(<BookmarkForm onSuccess={mockOnSuccess} onCancel={vi.fn()} />);

    fireEvent.change(screen.getByLabelText(/URL/i), { target: { value: 'https://example.com' } });
    fireEvent.click(screen.getByRole('button', { name: 'ブックマークを追加' }));

    await waitFor(() => {
      expect(bookmarkApi.createBookmark).toHaveBeenCalledTimes(1);
      expect(bookmarkApi.createBookmark).toHaveBeenCalledWith({
        url: 'https://example.com',
        title: undefined,
        description: undefined,
        tags: [],
      });
    });
    expect(mockOnSuccess).toHaveBeenCalledTimes(1);
  });

  it('APIエラーが発生した場合にエラーメッセージが表示されること', async () => {
    const errorMessage = 'ブックマークの追加に失敗しました。';
    vi.mocked(bookmarkApi.createBookmark).mockRejectedValue(new Error(errorMessage));

    render(<BookmarkForm onSuccess={vi.fn()} onCancel={vi.fn()} />);

    fireEvent.change(screen.getByLabelText(/URL/i), { target: { value: 'https://example.com' } });
    fireEvent.change(screen.getByLabelText(/タイトル/i), { target: { value: 'Test Title' } });
    fireEvent.click(screen.getByRole('button', { name: 'ブックマークを追加' }));

    await waitFor(() => {
      expect(screen.getByText(/ブックマークの追加に失敗しました。/)).toBeInTheDocument();
    });
    expect(bookmarkApi.createBookmark).toHaveBeenCalledTimes(1);
  });

  it('キャンセルボタンをクリックするとonCancelが呼び出されること', () => {
    const mockOnSuccess = vi.fn();
    const mockOnCancel = vi.fn();

    render(<BookmarkForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    fireEvent.click(screen.getByRole('button', { name: 'キャンセル' }));

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
    expect(mockOnSuccess).not.toHaveBeenCalled(); // onSuccessは呼び出されないことを確認
  });
});
