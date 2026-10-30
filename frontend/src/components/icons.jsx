// Iconos propios, trazo uniforme de 1.75 en rejilla de 24.
const base = {
  width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor',
  strokeWidth: 1.75, strokeLinecap: 'round', strokeLinejoin: 'round', 'aria-hidden': true,
}

export const BoltIcon = (p) => (
  <svg {...base} {...p}><path d="M13 3 5 13.5h6L10 21l8-10.5h-6L13 3Z" /></svg>
)
export const ChevronIcon = (p) => (
  <svg {...base} {...p}><path d="m9 6 6 6-6 6" /></svg>
)
export const AlertIcon = (p) => (
  <svg {...base} {...p}><path d="M12 4 3 20h18L12 4Z" /><path d="M12 10v4" /><path d="M12 17.2v.1" /></svg>
)
export const CheckIcon = (p) => (
  <svg {...base} {...p}><path d="m5 12.5 4.5 4.5L19 7.5" /></svg>
)
export const SpinnerIcon = (p) => (
  <svg {...base} {...p} className="spin"><path d="M12 3a9 9 0 1 0 9 9" /></svg>
)
