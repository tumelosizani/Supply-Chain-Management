export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

export interface AddressDTO {
  street?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
}

export interface ProductSummaryDTO {
  id: number;
  name: string;
  unitPrice: number;
  category: string;
}

export interface ProductRequestDTO {
  name: string;
  description: string;
  unitPrice: number;
  category: string;
}

export interface InventorySummaryDTO {
  id: number;
  productId: number;
  quantity: number;
}

export interface StockUpdateRequestDTO {
  productId: number;
  quantity: number;
}

export type CustomerOrderStatus =
  | 'NEW'
  | 'PENDING'
  | 'PROCESSING'
  | 'CONFIRMED'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';

export interface CustomerOrderItemResponseDTO {
  id: number;
  productId: number;
  quantity: number;
  unitPrice?: number;
}

export interface CustomerOrderResponseDTO {
  id: number;
  orderDate: string;
  status: CustomerOrderStatus;
  shippingAddress?: AddressDTO;
  items: CustomerOrderItemResponseDTO[];
  totalCost: number;
}

export type PurchaseOrderStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'PARTIALLY_RECEIVED'
  | 'FULLY_RECEIVED'
  | 'CANCELLED'
  | 'RETURNED';

export interface PurchaseOrderItemResponseDTO {
  id: number;
  productId: number;
  quantity: number;
  quantityReceived?: number;
  unitPrice?: number;
}

export interface PurchaseOrderResponseDTO {
  id: number;
  supplierId: number;
  orderDate: string;
  expectedDeliveryDate?: string;
  status: PurchaseOrderStatus;
  items: PurchaseOrderItemResponseDTO[];
}

export interface GoodsReceiptItemDTO {
  purchaseOrderItemId?: number;
  productId?: number;
  quantityReceived: number;
}

export interface GoodsReceiptRequestDTO {
  items: GoodsReceiptItemDTO[];
  notes?: string;
}

export interface SupplierResponseDTO {
  id: number;
  name: string;
  phoneNumber?: string;
  email?: string;
  address?: AddressDTO;
  website?: string;
  rating: number;
}

export interface SupplierRequestDTO {
  name: string;
  phoneNumber?: string;
  email?: string;
  website?: string;
  rating?: number;
}

export type ShipmentStatus = 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'RETURNED';

export interface ShipmentResponseDTO {
  id: number;
  orderId: number;
  trackingNumber?: string;
  carrier?: string;
  status: ShipmentStatus;
  shippedAt?: string;
  deliveredAt?: string;
}

export interface ShipmentDispatchRequestDTO {
  carrier: string;
  trackingNumber: string;
}
