export function SkeletonLine({ width = '100%', height = 14 }) {
  return <span className="skeleton" style={{ width, height }} />
}

export function SkeletonCard() {
  return (
    <div className="skeleton-card">
      <SkeletonLine width="30%" height={12} />
      <SkeletonLine width="55%" height={36} />
      <SkeletonLine width="100%" height={44} />
    </div>
  )
}

export function SkeletonRows({ rows = 4 }) {
  return (
    <div className="skeleton-rows">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="skeleton-row">
          <div className="skeleton-row-left">
            <SkeletonLine width="60%" height={13} />
            <SkeletonLine width="35%" height={11} />
          </div>
          <SkeletonLine width="80px" height={16} />
        </div>
      ))}
    </div>
  )
}
