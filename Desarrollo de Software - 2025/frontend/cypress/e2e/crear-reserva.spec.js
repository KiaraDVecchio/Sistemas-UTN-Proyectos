describe('E2E crear reserva Test', () => {
    it('Inicio sesion, creo reserva', () => {

        cy.visit('https://birbnb-test.lucasserral.com')

        cy.contains('button.call-to-action-button', '¡Inicia sesión o registrate!').click()
        cy.get('.modal').should('be.visible')

        cy.get('input[name="email"]').type('valen111@gmail.com') 
        cy.get('input[name="password"]').type('test')


        cy.contains('button', 'Iniciar sesión').click()
        cy.get('.modal').should('not.exist')

        cy.get('button#user-menu-dropdown-basic', { timeout: 10000 })
            .should('be.visible')
            .click()

        // Verificar que inicie sesion
        cy.get('.dropdown-menu:visible').first().within(() => {
            cy.contains('valen').should('exist')
            cy.contains('Mis reservas').should('exist')
            cy.contains('Cerrar sesión').should('exist')
        })


        cy.get('.alojamientos-carousel')
            .find('.alojamiento-card')
            .first()
            .click()


        cy.get('.w-100')
            .click()
        cy.get('.react-datepicker__day--020').click()


        cy.get('#fechaEnd').click()
        cy.get('.react-datepicker__day--025').click()
        


        cy.get('.botonDeReservar').click()

        cy.get('.btn-accion').click()
      

        cy.contains('.btn-accion', '🏠 Ir al menú de inicio').should('be.visible').click()

    })
})

