class Carousel {
    constructor() {
        this.currentSlide = 0;
        this.slides = document.querySelectorAll('.carousel-slide');
        this.indicators = document.querySelectorAll('.indicator');
        this.prevButton = document.querySelector('.carousel-control.prev');
        this.nextButton = document.querySelector('.carousel-control.next');
        this.autoPlayInterval = null;
        this.autoPlayDelay = 6000; // 6 seconds
        this.isPlaying = true;
        this.touchStartX = 0;
        this.touchEndX = 0;

        this.init();
    }

    init() {
        if (this.slides.length === 0) return;

        this.showSlide(0);

        this.prevButton?.addEventListener('click', () => this.previousSlide());
        this.nextButton?.addEventListener('click', () => this.nextSlide());

        this.indicators.forEach((indicator, index) => {
            indicator.addEventListener('click', () => this.showSlide(index));
        });

        document.addEventListener('keydown', (e) => {
            if (this.isCarouselVisible()) {
                if (e.key === 'ArrowLeft') {
                    e.preventDefault();
                    this.previousSlide();
                }
                if (e.key === 'ArrowRight') {
                    e.preventDefault();
                    this.nextSlide();
                }
                if (e.key === ' ') {
                    e.preventDefault();
                    this.toggleAutoPlay();
                }
            }
        });

        this.addTouchSupport();

        this.startAutoPlay();

        const container = document.querySelector('.carousel-container');
        container?.addEventListener('mouseenter', () => this.pauseAutoPlay());
        container?.addEventListener('mouseleave', () => this.resumeAutoPlay());

        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.pauseAutoPlay();
            } else {
                this.resumeAutoPlay();
            }
        });

        this.initializeSlideActions();
    }

    isCarouselVisible() {
        const carousel = document.querySelector('.hero-carousel');
        if (!carousel) return false;

        const rect = carousel.getBoundingClientRect();
        return rect.top < window.innerHeight && rect.bottom > 0;
    }

    showSlide(index) {
        this.slides.forEach((slide, i) => {
            slide.classList.remove('active');
            if (i === this.currentSlide && i !== index) {
                slide.classList.add('exiting');
                setTimeout(() => slide.classList.remove('exiting'), 1000);
            }
        });

        this.indicators.forEach(indicator => indicator.classList.remove('active'));

        if (index >= this.slides.length) index = 0;
        if (index < 0) index = this.slides.length - 1;

        this.slides[index]?.classList.add('active');
        this.indicators[index]?.classList.add('active');

        const prevIndex = this.currentSlide;
        this.currentSlide = index;

        this.animateSlideContent(index, prevIndex);

        this.updateBackgroundEffect();

        this.updateAccessibility();
    }

    animateSlideContent(newIndex, prevIndex) {
        const newSlide = this.slides[newIndex];
        const content = newSlide?.querySelector('.slide-content');

        if (content) {
            content.style.opacity = '0';
            content.style.transform = 'translateX(50px)';

            setTimeout(() => {
                content.style.transition = 'all 0.8s cubic-bezier(0.4, 0, 0.2, 1)';
                content.style.opacity = '1';
                content.style.transform = 'translateX(0)';
            }, 300);
        }
    }

    nextSlide() {
        this.showSlide(this.currentSlide + 1);
        this.resetAutoPlay();
    }

    previousSlide() {
        this.showSlide(this.currentSlide - 1);
        this.resetAutoPlay();
    }

    startAutoPlay() {
        if (this.slides.length <= 1) return;

        this.stopAutoPlay();
        this.isPlaying = true;
        this.autoPlayInterval = setInterval(() => {
            this.nextSlide();
        }, this.autoPlayDelay);
    }

    stopAutoPlay() {
        if (this.autoPlayInterval) {
            clearInterval(this.autoPlayInterval);
            this.autoPlayInterval = null;
        }
        this.isPlaying = false;
    }

    pauseAutoPlay() {
        this.stopAutoPlay();
    }

    resumeAutoPlay() {
        if (this.slides.length > 1) {
            this.startAutoPlay();
        }
    }

    resetAutoPlay() {
        if (this.isPlaying) {
            this.startAutoPlay();
        }
    }

    toggleAutoPlay() {
        if (this.isPlaying) {
            this.stopAutoPlay();
        } else {
            this.startAutoPlay();
        }
    }

    updateBackgroundEffect() {
        const activeSlide = this.slides[this.currentSlide];
        const backgroundImage = activeSlide?.querySelector('.slide-background');

        if (backgroundImage) {
            this.slides.forEach((slide, index) => {
                if (index !== this.currentSlide && slide.cleanupFunction) {
                    slide.cleanupFunction();
                    slide.cleanupFunction = null;
                }
            });

            const handleMouseMove = (e) => {
                if (!this.isCarouselVisible()) return;

                const { clientX, clientY } = e;
                const { innerWidth, innerHeight } = window;

                const xPos = (clientX / innerWidth - 0.5) * 15;
                const yPos = (clientY / innerHeight - 0.5) * 15;

                backgroundImage.style.transform = `scale(1.05) translate(${xPos}px, ${yPos}px)`;
            };

            let throttleTimer;
            const throttledMouseMove = (e) => {
                if (throttleTimer) return;
                throttleTimer = setTimeout(() => {
                    handleMouseMove(e);
                    throttleTimer = null;
                }, 16); // ~60fps
            };

            document.addEventListener('mousemove', throttledMouseMove);

            activeSlide.cleanupFunction = () => {
                document.removeEventListener('mousemove', throttledMouseMove);
                backgroundImage.style.transform = 'scale(1.05) translate(0, 0)';
            };
        }
    }

    updateAccessibility() {
        this.slides.forEach((slide, index) => {
            const isActive = index === this.currentSlide;
            slide.setAttribute('aria-hidden', !isActive);

            const buttons = slide.querySelectorAll('button');
            buttons.forEach(button => {
                button.tabIndex = isActive ? 0 : -1;
            });
        });

        this.indicators.forEach((indicator, index) => {
            const isActive = index === this.currentSlide;
            indicator.setAttribute('aria-pressed', isActive);
            indicator.setAttribute('aria-label', `Slide ${index + 1} of ${this.slides.length}`);
        });
    }

    addTouchSupport() {
        const container = document.querySelector('.carousel-container');
        if (!container) return;

        container.addEventListener('touchstart', (e) => {
            this.touchStartX = e.changedTouches[0].screenX;
            this.pauseAutoPlay();
        }, { passive: true });

        container.addEventListener('touchmove', (e) => {
            // Prevent scrolling during swipe
            if (Math.abs(e.changedTouches[0].screenX - this.touchStartX) > 10) {
                e.preventDefault();
            }
        });

        container.addEventListener('touchend', (e) => {
            this.touchEndX = e.changedTouches[0].screenX;
            this.handleSwipe();
            this.resumeAutoPlay();
        }, { passive: true });
    }

    handleSwipe() {
        const swipeThreshold = 50;
        const diff = this.touchStartX - this.touchEndX;

        if (Math.abs(diff) > swipeThreshold) {
            if (diff > 0) {
                this.nextSlide();
            } else {
                this.previousSlide();
            }
        }
    }

    initializeSlideActions() {
        document.querySelectorAll('.watch-trailer').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const slide = button.closest('.carousel-slide');
                const trailerUrl = this.getTrailerUrl(slide);

                if (trailerUrl) {
                    this.playTrailer(trailerUrl);
                } else {
                    this.showNotification('Trailer não disponível', 'error');
                }
            });
        });

        document.querySelectorAll('.add-watchlist').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const slide = button.closest('.carousel-slide');
                const movieId = slide?.getAttribute('data-movie-id');

                if (movieId) {
                    this.addToWatchlist(movieId);
                } else {
                    this.showNotification('ID do conteúdo não encontrado', 'error');
                }
            });
        });

        document.querySelectorAll('.btn-secondary').forEach(button => {
            if (button.textContent.includes('Ver Detalhes')) {
                button.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const slide = button.closest('.carousel-slide');
                    const movieId = slide?.getAttribute('data-movie-id');

                    if (movieId) {
                        window.location.href = `/movies/filmes/${movieId}`;
                    }
                });
            }
        });
    }

    getTrailerUrl(slide) {
        const trailerUrl = slide?.getAttribute('data-trailer');
        if (trailerUrl) return trailerUrl;

        if (window.movieData?.topFilmes) {
            const movieId = slide?.getAttribute('data-movie-id');
            const movie = window.movieData.topFilmes.find(f => f.id == movieId);
            return movie?.urlTrailer;
        }

        return null;
    }

    async playTrailer(trailerUrl) {
        try {
            this.pauseAutoPlay();

            if (window.movieUtils?.openTrailerModal) {
                window.movieUtils.openTrailerModal(trailerUrl);
            } else {
                this.createTrailerModal(trailerUrl);
            }
        } catch (error) {
            console.error('Error playing trailer:', error);
            this.showNotification('Erro ao reproduzir trailer', 'error');
            this.resumeAutoPlay();
        }
    }

    createTrailerModal(trailerUrl) {
        const modal = document.createElement('div');
        modal.className = 'trailer-modal';
        modal.innerHTML = `
            <div class="trailer-content">
                <button class="trailer-close" onclick="this.closest('.trailer-modal').remove(); carousel.resumeAutoPlay();">&times;</button>
                <div class="trailer-wrapper">
                    <iframe 
                        src="${this.convertToEmbedUrl(trailerUrl)}" 
                        title="Trailer"
                        frameborder="0" 
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                        allowfullscreen>
                    </iframe>
                </div>
            </div>
        `;

        document.body.appendChild(modal);
        document.body.style.overflow = 'hidden';

        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.remove();
                document.body.style.overflow = '';
                this.resumeAutoPlay();
            }
        });

        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                modal.remove();
                document.body.style.overflow = '';
                this.resumeAutoPlay();
                document.removeEventListener('keydown', handleEscape);
            }
        };
        document.addEventListener('keydown', handleEscape);
    }

    convertToEmbedUrl(url) {
        if (url.includes('youtube.com/watch')) {
            const videoId = url.split('v=')[1].split('&')[0];
            return `https://www.youtube.com/embed/${videoId}?autoplay=1`;
        }
        if (url.includes('youtu.be/')) {
            const videoId = url.split('youtu.be/')[1].split('?')[0];
            return `https://www.youtube.com/embed/${videoId}?autoplay=1`;
        }
        return url;
    }

    async addToWatchlist(movieId) {
        try {
            if (!window.movieData?.usuarioLogado) {
                if (window.authFunctions?.showSignInModal) {
                    window.authFunctions.showSignInModal();
                }
                return;
            }

            const response = await fetch(`/api/watchlist/${movieId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                this.showNotification('Adicionado à lista de favoritos!', 'success');
            } else if (response.status === 409) {
                this.showNotification('Já está na sua lista de favoritos', 'info');
            } else {
                this.showNotification('Erro ao adicionar à lista', 'error');
            }
        } catch (error) {
            console.error('Error adding to watchlist:', error);
            this.showNotification('Erro de conexão', 'error');
        }
    }

    showNotification(message, type = 'success') {
        if (window.movieUtils?.showNotification) {
            window.movieUtils.showNotification(message, type);
        } else {
            // Fallback notification
            console.log(`${type.toUpperCase()}: ${message}`);
        }
    }

    goToSlide(index) {
        this.showSlide(index);
    }

    play() {
        this.startAutoPlay();
    }

    pause() {
        this.stopAutoPlay();
    }

    destroy() {
        this.stopAutoPlay();

        this.slides.forEach(slide => {
            if (slide.cleanupFunction) {
                slide.cleanupFunction();
            }
        });
    }
}

let carousel;
document.addEventListener('DOMContentLoaded', () => {
    carousel = new Carousel();

    addProgressIndicator();

    setupIntersectionObserver();
});

function addProgressIndicator() {
    const carouselContainer = document.querySelector('.carousel-container');
    if (!carouselContainer) return;

    const progressBar = document.createElement('div');
    progressBar.className = 'carousel-progress';
    progressBar.innerHTML = '<div class="progress-fill"></div>';

    const style = document.createElement('style');
    style.textContent = `
        .carousel-progress {
            position: absolute;
            bottom: 0;
            left: 0;
            width: 100%;
            height: 3px;
            background: rgba(255,255,255,0.2);
            z-index: 4;
        }
        
        .progress-fill {
            height: 100%;
            background: var(--accent-red);
            width: 0%;
            transition: width 0.1s linear;
        }
        
        .carousel-progress.paused .progress-fill {
            animation-play-state: paused;
        }
    `;
    document.head.appendChild(style);

    carouselContainer.appendChild(progressBar);
}

function setupIntersectionObserver() {
    const carousel = document.querySelector('.hero-carousel');
    if (!carousel) return;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                // Carousel is visible, ensure autoplay is active
                if (window.carousel && !window.carousel.isPlaying) {
                    window.carousel.startAutoPlay();
                }
            } else {
                if (window.carousel) {
                    window.carousel.pauseAutoPlay();
                }
            }
        });
    }, { threshold: 0.1 });

    observer.observe(carousel);
}

window.carousel = carousel;

window.carouselUtils = {
    playTrailer: (movieId) => {
        if (carousel) {
            const slide = document.querySelector(`[data-movie-id="${movieId}"]`);
            const trailerUrl = carousel.getTrailerUrl(slide);
            if (trailerUrl) {
                carousel.playTrailer(trailerUrl);
            }
        }
    },

    addToWatchlist: (movieId) => {
        if (carousel) {
            carousel.addToWatchlist(movieId);
        }
    },

    goToSlide: (index) => {
        if (carousel) {
            carousel.goToSlide(index);
        }
    }
};

const enhancedStyle = document.createElement('style');
enhancedStyle.textContent = `
    .carousel-slide {
        transition: opacity 1.2s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .carousel-slide.exiting {
        opacity: 0;
        transform: scale(1.05);
    }
    
    .slide-content {
        transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .carousel-control {
        transition: all 0.3s ease;
        backdrop-filter: blur(10px);
    }
    
    .carousel-control:hover {
        background-color: rgba(229, 9, 20, 0.8);
        transform: translateY(-50%) scale(1.1);
        box-shadow: 0 8px 25px rgba(229, 9, 20, 0.3);
    }
    
    .carousel-control:active {
        transform: translateY(-50%) scale(0.95);
    }
    
    .indicator {
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .indicator:hover {
        background-color: rgba(255, 255, 255, 0.6);
        transform: scale(1.1);
    }
    
    .indicator.active {
        background-color: var(--accent-red);
        transform: scale(1.1);
        box-shadow: 0 2px 8px rgba(229, 9, 20, 0.4);
    }
    
    .btn-primary, .btn-secondary {
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;
    }
    
    .btn-primary::before, .btn-secondary::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        background: rgba(255,255,255,0.2);
        border-radius: 50%;
        transform: translate(-50%, -50%);
        transition: all 0.5s ease;
    }
    
    .btn-primary:hover::before, .btn-secondary:hover::before {
        width: 300px;
        height: 300px;
    }

    
    @media (max-width: 768px) {
        .carousel-control {
            width: 35px;
            height: 35px;
        }
        
        .carousel-control.prev {
            left: 1rem;
        }
        
        .carousel-control.next {
            right: 1rem;
        }
        
        .indicator {
            width: 30px;
            height: 3px;
        }
        
        .indicator.active {
            width: 45px;
        }
    }
    
    @media (prefers-reduced-motion: reduce) {
        .carousel-slide,
        .slide-content,
        .carousel-control,
        .indicator {
            transition: none;
        }
    }
`;
document.head.appendChild(enhancedStyle);