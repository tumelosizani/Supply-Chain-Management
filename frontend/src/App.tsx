import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { InventoryPage } from './pages/InventoryPage';
import { OrdersPage } from './pages/OrdersPage';
import { ProcurementPage } from './pages/ProcurementPage';
import { ProductsPage } from './pages/ProductsPage';
import { ShipmentsPage } from './pages/ShipmentsPage';
import { SuppliersPage } from './pages/SuppliersPage';

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/inventory" element={<InventoryPage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/shipments" element={<ShipmentsPage />} />
          <Route path="/procurement" element={<ProcurementPage />} />
          <Route path="/suppliers" element={<SuppliersPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
