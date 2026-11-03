export interface CommonResult<T> {
  code: number;
  data: T;
  details: any;
  message: string;
  success: boolean;
}
