import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './auth/ProtectedRoute';
import { useAuth, getHomeForRole } from './auth/store';

import Login from './pages/public/Login';
import LandingPage from './pages/public/LandingPage';
import Forbidden from './pages/Forbidden';
import PlaceholderPage from './pages/PlaceholderPage';

// admin
import AdminDashboard from './pages/admin/AdminDashboard';
import Users from './pages/admin/Users';
import RegisterStaff from './pages/admin/RegisterStaff';
import Audit from './pages/admin/Audit';
import ModelPortfolios from './pages/admin/ModelPortfolios';
import AdminSecurities from './pages/admin/AdminSecurities';

// rm
import Clients from './pages/rm/Clients';
import ClientDetail from './pages/rm/ClientDetail';
import OnboardClient from './pages/rm/OnboardClient';
import RmNotifications from './pages/rm/RmNotifications';
import RmRecommendations from './pages/rm/RmRecommendations';
import RmGoals from './pages/rm/RmGoals';
import CorporateActions from './pages/rm/CorporateActions';
import RmAnalytics from './pages/rm/RmAnalytics';

// client
import Dashboard from './pages/client/Dashboard';
import MyKyc from './pages/client/MyKyc';
import MyRiskProfile from './pages/client/MyRiskProfile';
import Products from './pages/client/Products';
import MyOrders from './pages/client/MyOrders';
import Holdings from './pages/client/Holdings';
import Goals from './pages/client/Goals';
import Notifications from './pages/client/Notifications';
import Reviews from './pages/client/Reviews';

// dealer
import OrderBlotter from './pages/dealer/OrderBlotter';
import OrderDetail from './pages/dealer/OrderDetail';
import Securities from './pages/dealer/Securities';
import ResearchNotes from './pages/dealer/ResearchNotes';
import ProductTerms from './pages/dealer/ProductTerms';

// compliance
import Breaches from './pages/compliance/Breaches';
import ComplianceAudit from './pages/compliance/ComplianceAudit';
import KYCApproval from './pages/compliance/KYCApproval';
import FailedOrders from './pages/compliance/FailedOrders';
import RiskMonitor from './pages/compliance/RiskMonitor';
import SuitabilityRules from './pages/compliance/SuitabilityRules';
import AmlFlags from './pages/compliance/AmlFlags';

function HomeRedirect() {
  const user = useAuth((s) => s.user);
  if (!user) return <LandingPage />;
  return <Navigate to={getHomeForRole(user.role)} />;
}

export default function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/" element={<HomeRedirect />} />
        <Route path="/login" element={<Login />} />
        <Route path="/403" element={<Forbidden />} />

        {/* ADMIN routes */}
        <Route element={<ProtectedRoute allow="ADMIN"><Layout /></ProtectedRoute>}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/users" element={<Users />} />
          <Route path="/admin/users/register" element={<RegisterStaff />} />
          <Route path="/admin/audit" element={<Audit />} />
          <Route path="/admin/model-portfolios" element={<ModelPortfolios />} />
          <Route path="/admin/securities" element={<AdminSecurities />} />
        </Route>

        {/* RM routes */}
        <Route element={<ProtectedRoute allow="RM"><Layout /></ProtectedRoute>}>
          <Route path="/rm/clients" element={<Clients />} />
          <Route path="/rm/clients/:id" element={<ClientDetail />} />
          <Route path="/rm/onboard" element={<OnboardClient />} />
          <Route path="/rm/recommendations" element={<RmRecommendations />} />
          <Route path="/rm/goals" element={<RmGoals />} />
          <Route path="/rm/analytics" element={<RmAnalytics />} />
          <Route path="/rm/notifications" element={<RmNotifications />} />
          <Route path="/rm/corporate-actions" element={<CorporateActions />} />
        </Route>

        {/* DEALER routes */}
        <Route element={<ProtectedRoute allow="DEALER"><Layout /></ProtectedRoute>}>
          <Route path="/dealer/orders" element={<OrderBlotter />} />
          <Route path="/dealer/orders/:id" element={<OrderDetail />} />
          <Route path="/dealer/securities" element={<Securities />} />
          <Route path="/dealer/research-notes" element={<ResearchNotes />} />
          <Route path="/dealer/product-terms" element={<ProductTerms />} />
        </Route>

        {/* COMPLIANCE routes */}
        <Route element={<ProtectedRoute allow="COMPLIANCE"><Layout /></ProtectedRoute>}>
          <Route path="/compliance/breaches"      element={<Breaches />} />
          <Route path="/compliance/kyc-approval"  element={<KYCApproval />} />
          <Route path="/compliance/failed-orders" element={<FailedOrders />} />
          <Route path="/compliance/risk-monitor"  element={<RiskMonitor />} />
          <Route path="/compliance/rules"         element={<SuitabilityRules />} />
          <Route path="/compliance/audit"         element={<ComplianceAudit />} />
          <Route path="/compliance/aml-flags"     element={<AmlFlags />} />
        </Route>

        {/* CLIENT routes */}
        <Route element={<ProtectedRoute allow="CLIENT"><Layout /></ProtectedRoute>}>
          <Route path="/me/dashboard" element={<Dashboard />} />
          <Route path="/me/kyc" element={<MyKyc />} />
          <Route path="/me/risk-profile" element={<MyRiskProfile />} />
          <Route path="/me/products" element={<Products />} />
          <Route path="/me/orders" element={<MyOrders />} />
          <Route path="/me/holdings" element={<Holdings />} />
          <Route path="/me/goals" element={<Goals />} />
          <Route path="/me/notifications" element={<Notifications />} />
          <Route path="/me/reviews" element={<Reviews />} />
        </Route>

        <Route path="*" element={<HomeRedirect />} />
      </Routes>
    </BrowserRouter>
  );
}
