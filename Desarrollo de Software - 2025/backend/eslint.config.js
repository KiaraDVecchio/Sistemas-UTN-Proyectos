import js from "@eslint/js"
import globals from "globals"
import { defineConfig } from "eslint/config"
import stylisticJs from "@stylistic/eslint-plugin-js"

export default defineConfig([
  {
    files: ["**/*.{js,mjs,cjs}"],
    plugins: { js, "@stylistic/js": stylisticJs },
    extends: ["js/recommended"],
    rules: {
      "@stylistic/js/semi": ["error", "never"],
      "no-unused-vars": [
        "error",
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
          ignoreRestSiblings: true,
        },
      ],
    },
  },
  {
    files: ["**/*.{js,mjs,cjs}"],
    languageOptions: { globals: globals.node },
  },
  {
    files: ["**/*.test.{js,mjs,cjs}"],
    languageOptions: { globals: globals.jest },
  },
])