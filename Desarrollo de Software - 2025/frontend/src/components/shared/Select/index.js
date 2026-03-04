import { useLayoutEffect, useMemo, useRef, useState } from 'react'
import '../Input/styles.css'
import './styles.css'
import { Dropdown } from '../Dropdown'
import { ArrowIcon } from '../ArrowIcon'
import { Input } from '../Input'

export const Select = ({ options, label, onChange, style, value, nowrap, searchable, multiple, disabled }) => {
  const [search, setSearch] = useState('')

  const display = Array.isArray(value)
    ? value.map((v, index) => `${options?.find(el => String(el.value) === String(v))?.label ?? ''}${index !== value.length - 1 ? ', ' : ''}`)
    : options?.find(el => String(el.value) === String(value))?.label

  const dropdownRef = useRef(null)
  const [open, setOpen] = useState(false)

  useLayoutEffect(() => {
    setSearch('')
    function closeModal(evt) {
      if (dropdownRef.current != null && !dropdownRef.current.contains(evt.target)) {
        setOpen(false)
      }
    }

    setTimeout(() => {
      if (dropdownRef.current != null && open) {
        window.addEventListener('click', closeModal)
      }
    }, 500)
    return () => {
      window.removeEventListener('click', closeModal)
    }
  }, [open, dropdownRef.current])

  const filteredOptions = useMemo(() => options?.filter(o => o.label.split(' ').join('').toLocaleLowerCase().includes(search.split(' ').join('').toLocaleLowerCase())) ?? [], [search, options])

  return (
    <div className={`${'uiinput__containerContainer'} ${disabled === true ? 'uiinput__disabled' : ''} ${window._screenType === 'mobile' ? 'uiinput__mobile' : ''}`} style={style?.containerContainer}>
      <div
        style={style?.container}
        className={`${'uiinput__container'} ${'uiselect__container'}`} onClick={() => {
          if (disabled !== true) {
            setOpen(true)
          }
        }}
      >
        <div className={'uiinput__label'}>
          {label}
        </div>
        <div style={nowrap === true ? { ...style?.input ?? {}, overflow: 'hidden', textOverflow: 'ellipsis', textAlign: 'left', whiteSpace: 'nowrap' } : style?.input} className={`${'uiinput__input'} ${'uiselect__text'}`}>{display}</div>
        <div style={{ transform: 'rotate(90deg) scale(0.7)' }}>
          <ArrowIcon color={disabled === true ? 'gray' : 'gray'} />
        </div>
        <div style={{
          position: 'absolute',
          top: '36px',
          left: 0,
          width: '100%'
        }}
        >
          <Dropdown style={style?.dropdown} ref={dropdownRef} open={open}>
            {searchable === true && <div><Input.Secondary label='Buscar' style={{ container: { width: '100%', minWidth: 0 }, containerContainer: { width: '100%', padding: '8px', boxSizing: 'border-box', marginTop: '4px' } }} value={search} onChange={(evt) => setSearch(evt.target.value)} /></div>}
            {
              filteredOptions.map(_o => {
                return (
                  <div
                    style={multiple === true && Array.isArray(value) && value.includes(_o.value) ? { background: 'aliceblue' } : {}}
                    className={'uiselect__dropdownItem'}
                    key={_o.value}
                    onClick={(evt) => {
                      evt.stopPropagation()
                      setOpen(false)
                      if (multiple !== true && onChange != null) {
                        onChange(_o.value)
                      } else if (multiple === true && onChange != null) {
                        if (Array.isArray(value)) {
                          if (value.includes(_o.value)) {
                            onChange([...value.filter(el => el !== _o.value)])
                          } else {
                            onChange([...value, _o.value])
                          }
                        } else {
                          onChange([_o.value])
                        }
                      }
                    }}
                  >{_o.label}
                  </div>
                )
              })
            }
          </Dropdown>
        </div>
      </div>
    </div>
  )
}
