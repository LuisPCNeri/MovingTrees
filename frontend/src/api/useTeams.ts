import { useEffect, useState } from 'react'
import { listTeams, type ApiError, type Team } from './users'

/** The caller's municipality teams, loaded once per page. */
export function useTeams() {
  const [teams, setTeams] = useState<Team[] | null>(null)
  const [error, setError] = useState<ApiError | null>(null)

  useEffect(() => {
    const ctrl = new AbortController()
    listTeams(ctrl.signal)
      .then(setTeams)
      .catch((e: ApiError) => e.name !== 'AbortError' && setError(e))
    return () => ctrl.abort()
  }, [])

  const options: [string, string][] = (teams ?? []).map((t) => [String(t.id), t.name])
  const nameOf = (id: number) => teams?.find((t) => t.id === id)?.name ?? `Team #${id}`
  return { teams, error, options, nameOf }
}
