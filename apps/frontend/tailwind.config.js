/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'light-orange': '#FFF3E0', // 淡いオレンジ
      },
    },
  },
  plugins: [],
}
