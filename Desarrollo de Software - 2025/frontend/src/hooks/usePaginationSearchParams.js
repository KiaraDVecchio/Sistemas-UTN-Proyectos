import { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'

export function usePaginationSearchParams(totalPages) {

    const [searchparams, setSearchParam] = useSearchParams()

    const currentPage = Number(searchparams.get('page') ?? '1')
    const prevPages = [currentPage - 3, currentPage - 2, currentPage - 1].filter(el => el >= 1)
    const nextPages = [currentPage + 1, currentPage + 2, currentPage + 3].filter(el => el <= (totalPages ?? 1))

    useEffect(() => {
        if (totalPages != null) {
            if ((totalPages - 1) < currentPage) {
                setSearchParam(prev => {
                    prev.set('page', String(totalPages))
                    return prev
                })
            }
        }
    }, [totalPages])

    function gotoPrevPage() {
        setSearchParam(prev => {
            prev.set('page', String(currentPage - 1))
            return prev
        })
    }

    function gotoNextPage() {
        setSearchParam(prev => {
            prev.set('page', String(currentPage + 1))
            return prev
        })
    }

    function gotoPage(pageNumber) {
        setSearchParam(prev => {
            prev.set('page', String(pageNumber))
            return prev
        })
    }

    return {
        actions: {
            gotoPrevPage,
            gotoNextPage,
            gotoPage
        },
        state: {
            currentPage, prevPages, nextPages
        }
    }
}