export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        paprika: {
          50: "#fff7ed",
          100: "#ffedd5",
          300: "#fdba74",
          500: "#ea580c",
          700: "#9a3412",
          900: "#7c2d12"
        },
        sand: {
          50: "#f8f6ef",
          100: "#f3ece0",
          300: "#dcc9ad",
          500: "#b79b78",
          700: "#785f43",
          900: "#36291f"
        }
      },
      fontFamily: {
        display: ["Georgia", "serif"],
        body: ["Segoe UI", "sans-serif"]
      },
      boxShadow: {
        glow: "0 20px 45px rgba(154, 52, 18, 0.18)"
      }
    }
  },
  plugins: []
};
