import { ForbiddenPage } from './pages/ForbiddenPage'
import { LoginPage } from './pages/LoginPage'
import { NewUserPage } from './pages/admin/NewUserPage'
import { UserDetailPage } from './pages/admin/UserDetailPage'
import { UsersPage } from './pages/admin/UsersPage'

// shortcut: path switch with plain <a> links (full reloads), move to a router when navigation grows
export default function App() {
  const path = location.pathname
  if (path === '/forbidden') return <ForbiddenPage />
  if (path === '/admin/users') return <UsersPage />
  if (path === '/admin/users/new') return <NewUserPage />
  const detail = path.match(/^\/admin\/users\/([^/]+)$/)
  if (detail) return <UserDetailPage id={detail[1]} />
  return <LoginPage />
}
