import { useAlojamientosTotalPages } from '../../context/Alojamientos'
import { usePaginationSearchParams } from '../../hooks/usePaginationSearchParams'

export const Pager = ({ totalPages }) => {

    const {
        state: { currentPage, nextPages, prevPages },
        actions: { gotoNextPage, gotoPage, gotoPrevPage }
    } = usePaginationSearchParams(totalPages)

    return (
        <div style={{ color: 'var(--azulito)', display: 'flex', flexWrap: 'nowrap', gap: '8px', userSelect: 'none', marginBottom: "8px", justifyContent: 'center', width: '100%', margin: 'auto 0 0' }}>
            {currentPage > 1 && <div style={{ cursor: 'pointer' }} onClick={() => gotoPrevPage()}>{'<'}</div>}
            {prevPages.map(el => <div key={el} style={{ cursor: 'pointer' }} onClick={() => gotoPage(el)}>{el}</div>)}
            <div style={{ textDecoration: 'underline', fontWeight: 600 }}>{currentPage}</div>
            {nextPages.map(el => <div key={el} style={{ cursor: 'pointer' }} onClick={() => gotoPage(el)}>{el}</div>)}
            {currentPage < (totalPages ?? 1) && <div style={{ cursor: 'pointer' }} onClick={() => gotoNextPage()}>{'>'}</div>}
        </div>
    )
}
