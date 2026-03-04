describe('E2E Login Modal Test', () => {
  it('Abre el modal, completa el formulario y se cierra al enviar', () => {
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

    // Verificar opciones en menú desplegado
    cy.get('.dropdown-menu:visible').first().within(() => {
      cy.contains('valen').should('exist')
      cy.contains('Mis reservas').should('exist')
      cy.contains('Cerrar sesión').should('exist')
    })
  })
  it('Muestra toast de error si las credenciales son incorrectas', () => {
    cy.visit('https://birbnb-test.lucasserral.com')

    cy.contains('button.call-to-action-button', '¡Inicia sesión o registrate!').click()

    cy.get('.modal').should('be.visible')

    cy.get('input[name="email"]').type('usuario_incorrecto@gmail.com')
    cy.get('input[name="password"]').type('mal')

    cy.contains('button', 'Iniciar sesión').click()

    cy.get('.toast')
      .should('be.visible')
      .and('contain.text', 'Ocurrió un error al crear un usuario')

    cy.get('.modal').should('be.visible') // y el modal sigue abierto
  })

})

