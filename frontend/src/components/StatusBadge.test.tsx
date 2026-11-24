import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { StatusBadge } from './StatusBadge'

describe('StatusBadge', () => {
  it('renders status with underscores replaced by spaces', () => {
    render(<StatusBadge status="PARTIALLY_RECEIVED" />)

    expect(screen.getByText('PARTIALLY RECEIVED')).toBeInTheDocument()
  })

  it('falls back to default styling for unknown statuses', () => {
    render(<StatusBadge status="CUSTOM_STATUS" />)

    const badge = screen.getByText('CUSTOM STATUS')
    expect(badge).toHaveClass('bg-slate-100')
  })
})
