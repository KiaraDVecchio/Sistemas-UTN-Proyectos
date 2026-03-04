import React from 'react'
import './styles.css'


export const Dropdown = React.forwardRef(({ children, open, style }, ref) => {
  return (
    <div style={style} className={`${'uidropdown__container'} ${open === true ? 'uidropdown__open' : ''}`} ref={ref}>
      {children}
    </div>
  )
})