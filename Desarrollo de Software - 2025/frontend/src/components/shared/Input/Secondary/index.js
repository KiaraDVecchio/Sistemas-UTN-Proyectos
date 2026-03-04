import { JSX, useMemo } from 'react'
import './styles.css'


export const InputSecondary = ({ id, style, name, value, label, required, onChange, isPassword = false, readonly }) => {

  return (
    <div className={'uiinputsecondary__containerContainer'} style={style?.containerContainer}>
      <div style={style?.container} className={`${'uiinputsecondary__container'}`}>
        <div style={style?.label} className={`${'uiinputsecondary__label'} `}>
          {label}
        </div>
        <input readOnly={readonly} type={isPassword ? 'password' : 'text'} style={style?.input} className={`${'uiinputsecondary__input'} `} value={value} onChange={onChange} required={required} name={name} id={id} />
      </div>
    </div>
  )
}

export const TextArea = ({ id, style, name, value, label, required, onChange, validations = [], readonly }) => {

  return (
    <div>
      <div style={style?.container} className={`${'uiinputsecondary__container'}`}>
        <div style={style?.label} className={`${'uiinputsecondary__label'}`}>
          {label}
        </div>
        <textarea readOnly={readonly} style={style?.input} className={`${'uiinputsecondary__textarea'}`} value={value} onChange={onChange} required={required} name={name} id={id} />
      </div>
    </div>
  )
}

