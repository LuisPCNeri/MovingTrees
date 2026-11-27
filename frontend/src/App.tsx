import { NewUserPage } from './pages/admin/NewUserPage'
import { UsersPage } from './pages/admin/UsersPage'

// shortcut: path switch with plain <a> links (full reloads), move to a router when navigation grows
export default function App() {
  if (location.pathname === '/admin/users/new') return <NewUserPage />
  return <UsersPage />
}
