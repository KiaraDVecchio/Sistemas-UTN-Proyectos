import React from 'react'
import './styles.css'


export const ArrowIcon = ({ direction = 'right', color }) => {
  return (
    <div className={'arrowIcon__container'}>
      <div style={color != null ? { backgroundColor: color } : {}}
        className={
          `${'arrowIcon__line'}
          ${'arrowIcon__first'}
          ${direction === 'right' ? 'arrowIcon__degPlus' : 'arrowIcon__degMinus'}`
        } />

      <div style={color != null ? { backgroundColor: color } : {}}
        className={
          `${'arrowIcon__line'}
        ${'arrowIcon__second'}
        ${direction === 'right' ? 'arrowIcon__degMinus' : 'arrowIcon__degPlus'}`
        } />
    </div>
  )
}
