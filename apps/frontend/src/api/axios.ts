import axios from 'axios';

const API_BASE_URL = 'https://mock.echoapi.com/mock/3ddd7080b35017';

import { type ErrorResponse } from './types';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// エラーハンドリングのインターセプター
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // サーバーからのエラーレスポンスがある場合
      const errorData: ErrorResponse = error.response.data;
      console.error('API Error:', errorData.errorCode, errorData.message);
      // 必要に応じて、ここで特定のエラーコードに対する処理を追加
    } else if (error.request) {
      // リクエストは送信されたが、レスポンスがない場合
      console.error('No response received:', error.request);
    } else {
      // リクエストの設定中にエラーが発生した場合
      console.error('Error setting up request:', error.message);
    }
    return Promise.reject(error);
  }
);

export default apiClient;
