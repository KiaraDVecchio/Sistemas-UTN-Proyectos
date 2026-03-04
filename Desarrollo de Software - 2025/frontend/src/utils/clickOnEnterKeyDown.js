export const clickOnEnterKeyDown = (e) => {
    if (e.key === 'Enter') {
        e.target.click()
    }
}