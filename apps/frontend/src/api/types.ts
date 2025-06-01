export interface BookmarkInput {
  url: string;
  title?: string;
  description?: string;
  tags?: string[];
}

export interface BookmarkResponse {
  id: number;
  url: string;
  title: string | null;
  description: string | null;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ErrorResponse {
  errorCode: string;
  message: string;
}
