/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'airline-primary': '#003366',
        'airline-secondary': '#0066CC',
        'airline-accent': '#FFD700',
        'airline-orange': '#FF6B35',
        'airline-sky': '#87CEEB',
        'airline-sky-dark': '#4682B4',
      },
      fontFamily: {
        'sans': ['Inter', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        'airline-soft': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        'airline-medium': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
        'airline-large': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
      },
      backgroundImage: {
        'airline-primary': 'linear-gradient(135deg, #003366 0%, #0066CC 100%)',
        'airline-sunset': 'linear-gradient(135deg, #FF6B35 0%, #FFD700 100%)',
        'airline-sky': 'linear-gradient(180deg, #87CEEB 0%, #4682B4 100%)',
      },
    },
  },
  plugins: [],
}

