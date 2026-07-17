export interface TraceProduct {
  productName: string
  batchNo: string
  origin?: string
  produceDate?: string
  shelfLife?: number
  quantity?: number
  unit?: string
  status?: number
}

export interface TraceNode {
  traceId?: number
  operation: string
  operationLabel: string
  operator: string
  location?: string
  temp?: number
  humidity?: number
  opTime?: string
  txHash?: string
}

export interface TraceChain {
  txHash?: string
  blockNumber?: number
  chainTime?: string
  confirmations?: number
  chainStatus?: string
  dataHash?: string
}

export interface TraceQueryResult {
  product: TraceProduct
  timeline: TraceNode[]
  blockchain: TraceChain
}

export interface TraceVerifyResult {
  valid: boolean
  message: string
  batchNo?: string
  txHash?: string
  dataHash?: string
  blockNumber?: number
}
