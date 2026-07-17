export interface ProductBatch {
  batchId: number
  batchNo: string
  productName: string
  origin?: string
  producerId?: number
  produceDate?: string
  shelfLife?: number
  quantity?: number
  unit?: string
  qrCode?: string
  txHash?: string
  status: number
  createTime?: string
}

export interface BatchCreateResult {
  batchId: number
  batchNo: string
  qrCodeUrl: string
  txHash: string
  status: string
}

export interface QrCodeInfo {
  batchId: number
  batchNo: string
  qrContent: string
  qrCodeUrl: string
  txHash?: string
}
