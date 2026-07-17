import { get, post } from '@/utils/request'
import type { PageResult } from '@/types/api'

export interface ChainOverview {
  currentBlock: number
  totalTx: number
  nodeCount: number
  consensus: string
  networkStatus: string
}

export interface ChainTx {
  txId: number
  txHash: string
  blockNumber: number
  bizType: string
  bizId: string
  dataHash: string
  chainStatus: string
  createTime: string
  confirmations: number
}

export interface ChainTxPage extends PageResult<ChainTx> {
  overview?: ChainOverview
}

export interface ChainNode {
  id: string
  name: string
  role: string
}

export function fetchBlockchainOverview() {
  return get<ChainOverview>('/blockchain/overview')
}

export function fetchBlockchainTxs(params: Record<string, unknown>) {
  return get<ChainTxPage>('/blockchain/txs', params)
}

export function verifyBlockchain(data: { dataHash?: string; txHash?: string; originalData?: string }) {
  return post<{ verified: boolean; txHash?: string; blockNumber?: number; message: string }>(
    '/blockchain/verify',
    data,
  )
}

export function fetchBlockchainTopology() {
  return get<ChainNode[]>('/blockchain/topology')
}
