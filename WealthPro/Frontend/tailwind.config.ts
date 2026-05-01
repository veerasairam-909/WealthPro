import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // ───── WealthPro palette (Zerodha Kite + Groww inspired) ─────
        bg: '#FFFFFF',
        surface: {
          DEFAULT: '#FAFBFC',
          2: '#F4F6F8',
        },
        border: {
          DEFAULT: '#E5E7EB',
          hairline: '#EEF1F4',
        },
        text: {
          DEFAULT: '#1A1F36',
          2: '#5C6B82',
          3: '#98A2B3',
        },
        primary: {
          DEFAULT: '#387ED1',  // Kite blue
          dark:    '#2C68B0',
          soft:    '#EAF2FB',
        },
        success: {
          DEFAULT: '#00B386',  // Groww green
          soft:    '#E0F7EF',
        },
        danger: {
          DEFAULT: '#EB5B3C',  // Kite red
          soft:    '#FCEAE5',
        },
        warn: {
          DEFAULT: '#F4A41E',
          soft:    '#FFF4DD',
        },
        accent: {
          DEFAULT: '#5367FF',
          soft:    '#ECEEFF',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'ui-monospace', 'monospace'],
      },
      fontSize: {
        xs:  ['11px', { lineHeight: '1.45' }],
        sm:  ['13px', { lineHeight: '1.5' }],
        base:['14px', { lineHeight: '1.5' }],
        lg:  ['16px', { lineHeight: '1.4' }],
        xl:  ['18px', { lineHeight: '1.35' }],
        '2xl': ['22px', { lineHeight: '1.25' }],
        '3xl': ['28px', { lineHeight: '1.2' }],
      },
      borderRadius: {
        sm: '6px',
        DEFAULT: '8px',
        lg: '12px',
        xl: '14px',
      },
      boxShadow: {
        none: 'none',
      },
    },
  },
  plugins: [],
} satisfies Config;
