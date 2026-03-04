const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    baseUrl: "https://birbnb-test.lucasserral.com",
    specPattern: "cypress/e2e/**/*.js", 
    setupNodeEvents(on, config) {
    },
  },
});
