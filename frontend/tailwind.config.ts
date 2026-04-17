export default {
    content: [
        "./app/**/*.{vue,js,ts}"
    ],
    theme: {
        extend: {
            animation: {
                'indeterminate': 'indeterminate 1.5s infinite ease-in-out',
            },
            keyframes: {
                indeterminate: {
                    '0%': { transform: 'translateX(-100%) scaleX(0.3)' },
                    '50%': { transform: 'translateX(0%) scaleX(0.5)' },
                    '100%': { transform: 'translateX(100%) scaleX(0.3)' },
                }
            }
        }
    },
    plugins: []
}
