import apiClient from './axios';
import { type BookmarkInput, type BookmarkResponse } from './types';
import { AxiosError } from 'axios';

export const createBookmark = async (data: BookmarkInput): Promise<BookmarkResponse> => {
  try {
    const response = await apiClient.post<BookmarkResponse>('/bookmarks', data);
    return response.data;
  } catch (error: unknown) {
    if (error instanceof AxiosError) {
      throw new Error(error.response?.data?.message || 'ブックマークの作成に失敗しました。');
    }
    throw new Error('不明なエラーが発生しました。');
  }
};

export const listBookmarks = async (limit: number = 10, offset: number = 0): Promise<BookmarkResponse[]> => {
  try {
    const response = await apiClient.get<BookmarkResponse[]>('/bookmarks', {
      params: { limit, offset },
    });
    return response.data;
  } catch (error: unknown) {
    if (error instanceof AxiosError) {
      throw new Error(error.response?.data?.message || 'ブックマークの取得に失敗しました。');
    }
    throw new Error('不明なエラーが発生しました。');
  }
};

export const getBookmarkById = async (id: number): Promise<BookmarkResponse> => {
  try {
    const response = await apiClient.get<BookmarkResponse>(`/bookmarks/${id}`);
    return response.data;
  } catch (error: unknown) {
    if (error instanceof AxiosError) {
      throw new Error(error.response?.data?.message || '指定されたブックマークの取得に失敗しました。');
    }
    throw new Error('不明なエラーが発生しました。');
  }
};

export const updateBookmark = async (id: number, data: BookmarkInput): Promise<BookmarkResponse> => {
  try {
    const response = await apiClient.put<BookmarkResponse>(`/bookmarks/${id}`, data);
    return response.data;
  } catch (error: unknown) {
    if (error instanceof AxiosError) {
      throw new Error(error.response?.data?.message || 'ブックマークの更新に失敗しました。');
    }
    throw new Error('不明なエラーが発生しました。');
  }
};

export const deleteBookmark = async (id: number): Promise<void> => {
  try {
    await apiClient.delete(`/bookmarks/${id}`);
  } catch (error: unknown) {
    if (error instanceof AxiosError) {
      throw new Error(error.response?.data?.message || 'ブックマークの削除に失敗しました。');
    }
    throw new Error('不明なエラーが発生しました。');
  }
};
