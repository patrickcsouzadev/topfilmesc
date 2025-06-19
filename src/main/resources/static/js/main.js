document.addEventListener('DOMContentLoaded', function() {
    initializeHeader();
    initializeMovieCards();
    initializeSearch();
    initializeRatingStars();
    initializeUserMenu();
});

const API_CONFIG = {
    baseURL: window.location.origin,
    endpoints: {
        filmes: '/movies/api/filmes',
        series: '/movies/api/series',
        reviews: '/reviews/api',
        ratings: '/ratings/api',
        usuarios: '/usuarios/api',
        login: '/usuarios/api/login'
    }
};

class ApiService {
    static async request(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
            },
        };

        const config = { ...defaultOptions, ...options };

        try {
            const response = await fetch(url, config);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }

            return response;
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    static async get(endpoint, params = {}) {
        const url = new URL(API_CONFIG.baseURL + endpoint);
        Object.keys(params).forEach(key => {
            if (params[key] !== null && params[key] !== undefined) {
                url.searchParams.append(key, params[key]);
            }
        });

        return this.request(url.toString());
    }

    static async post(endpoint, data) {
        return this.request(API_CONFIG.baseURL + endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    static async put(endpoint, data) {
        return this.request(API_CONFIG.baseURL + endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    static async delete(endpoint) {
        return this.request(API_CONFIG.baseURL + endpoint, {
            method: 'DELETE'
        });
    }
}

class MovieService {
    static async getFilmes(filtros = {}) {
        return ApiService.get(API_CONFIG.endpoints.filmes, filtros);
    }

    static async getFilme(id) {
        return ApiService.get(`${API_CONFIG.endpoints.filmes}/${id}`);
    }

    static async getSeries(filtros = {}) {
        return ApiService.get(API_CONFIG.endpoints.series, filtros);
    }

    static async getSerie(id) {
        return ApiService.get(`${API_CONFIG.endpoints.series}/${id}`);
    }

    static async criarFilme(dadosFilme) {
        return ApiService.post(API_CONFIG.endpoints.filmes, dadosFilme);
    }

    static async criarSerie(dadosSerie) {
        return ApiService.post(API_CONFIG.endpoints.series, dadosSerie);
    }
}

class ReviewService {
    static async getReviewsFilme(filmeId) {
        return ApiService.get(`${API_CONFIG.endpoints.reviews}/filme/${filmeId}`);
    }

    static async getReviewsSerie(serieId) {
        return ApiService.get(`${API_CONFIG.endpoints.reviews}/serie/${serieId}`);
    }

    static async criarReviewFilme(filmeId, dadosReview) {
        return ApiService.post(`${API_CONFIG.endpoints.reviews}/filme/${filmeId}`, dadosReview);
    }

    static async criarReviewSerie(serieId, dadosReview) {
        return ApiService.post(`${API_CONFIG.endpoints.reviews}/serie/${serieId}`, dadosReview);
    }
}

class RatingService {
    static async getRatingFilme(filmeId) {
        return ApiService.get(`/ratings/api/filme/${filmeId}/media`);
    }

    static async getRatingSerie(serieId) {
        return ApiService.get(`/ratings/api/serie/${serieId}/media`);
    }

    static async avaliarFilme(filmeId, dadosRating) {
        return ApiService.post(`/ratings/api/filme/${filmeId}`, dadosRating);
    }

    static async avaliarSerie(serieId, dadosRating) {
        return ApiService.post(`/ratings/api/serie/${serieId}`, dadosRating);
    }
}

class UserService {
    static async login(credentials) {
        return ApiService.post(API_CONFIG.endpoints.login, credentials);
    }

    static async registrar(dadosUsuario) {
        return ApiService.post(API_CONFIG.endpoints.usuarios, dadosUsuario);
    }

    static async getUsuario(id) {
        return ApiService.get(`${API_CONFIG.endpoints.usuarios}/${id}`);
    }
}

function initializeHeader() {
    const header = document.querySelector('.header');
    let lastScroll = 0;

    window.addEventListener('scroll', () => {
        const currentScroll = window.pageYOffset;

        if (currentScroll <= 0) {
            header.style.backgroundColor = 'rgba(10, 10, 10, 0.95)';
        } else if (currentScroll > lastScroll) {
            header.style.transform = 'translateY(-100%)';
        } else {
            header.style.transform = 'translateY(0)';
            header.style.backgroundColor = 'rgba(10, 10, 10, 0.98)';
        }

        lastScroll = currentScroll;
    });
}

function initializeMovieCards() {
    document.body.addEventListener('click', async function(event) {

        const deleteButton = event.target.closest('.btn-delete-card');
        if (deleteButton) {
            event.stopPropagation();
            const card = deleteButton.closest('.movie-card');
            const contentId = card.getAttribute('data-movie-id') || card.getAttribute('data-show-id');
            const contentType = card.getAttribute('data-content-type');

            if (confirm(`Tem certeza que deseja deletar este ${contentType}? Esta ação não pode ser desfeita.`)) {
                try {
                    const endpoint = `/admin/api/${contentType === 'filme' ? 'filmes' : 'series'}/${contentId}`;
                    const response = await fetch(endpoint, { method: 'DELETE' });
                    const result = await response.json();
                    if (result.success) {
                        alert(result.message || 'Conteúdo deletado com sucesso!');
                        card.remove();
                    } else {
                        alert('Erro ao deletar: ' + result.error);
                    }
                } catch (err) {
                    alert('Erro de conexão ao tentar deletar.');
                }
            }
            return;
        }

        const card = event.target.closest('.movie-card');
        if (card) {
            if (event.target.closest('.play-btn')) {
                return;
            }
            const movieId = card.getAttribute('data-movie-id');
            const showId = card.getAttribute('data-show-id');
            const contentType = card.getAttribute('data-content-type');
            const finalId = (movieId && movieId !== 'null') ? movieId : showId;

            if (finalId && contentType && typeof openMovieModal === 'function') {
                openMovieModal(finalId, contentType);
            }
        }
    });

    const playButtons = document.querySelectorAll('.play-btn');
    playButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation(); // Impede que o modal abra quando clicar no play
            const card = this.closest('.movie-card');
            const movieId = card.getAttribute('data-movie-id') || card.getAttribute('data-show-id');

            if (movieId) {
                playTrailer(movieId);
            }
        });
    });

    document.querySelectorAll('.add-watchlist').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const movieId = this.getAttribute('data-movie-id') ||
                this.closest('.movie-card')?.getAttribute('data-movie-id') ||
                this.closest('.movie-card')?.getAttribute('data-show-id') ||
                this.closest('.carousel-slide')?.getAttribute('data-movie-id');

            if (movieId && window.carouselUtils?.addToWatchlist) {
                window.carouselUtils.addToWatchlist(movieId);
            } else if (movieId) {
                addToWatchlistDirect(movieId);
            }
        });
    });
}

async function addToWatchlistDirect(movieId) {
    try {
        if (!window.movieData?.usuarioLogado) {
            if (window.authFunctions?.showSignInModal) {
                window.authFunctions.showSignInModal();
            } else {
                alert('Faça login para adicionar à lista de favoritos');
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
            showNotification('Adicionado à lista de favoritos!', 'success');
        } else if (response.status === 409) {
            showNotification('Já está na sua lista de favoritos', 'info');
        } else {
            showNotification('Erro ao adicionar à lista', 'error');
        }
    } catch (error) {
        console.error('Error adding to watchlist:', error);
        showNotification('Erro de conexão', 'error');
    }
}

function initializeSearch() {
    const searchInput = document.querySelector('.search-input');
    const searchBtn = document.querySelector('.search-btn');
    let searchTimeout;

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                performSearch(this.value);
            }, 300);
        });

        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch(this.value);
            }
        });
    }

    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const searchValue = searchInput?.value || '';
            performSearch(searchValue);
        });
    }
}

async function performSearch(query) {
    if (query.trim() === '') return;

    try {
        showSearchLoading();

        const [filmesResults, seriesResults] = await Promise.all([
            MovieService.getFilmes({ titulo: query }),
            MovieService.getSeries({ titulo: query })
        ]);

        displaySearchResults(filmesResults, seriesResults, query);
    } catch (error) {
        console.error('Erro na busca:', error);
        showSearchError();
    }
}

function showSearchLoading() {
    let loadingDiv = document.getElementById('searchLoading');
    if (!loadingDiv) {
        loadingDiv = document.createElement('div');
        loadingDiv.id = 'searchLoading';
        loadingDiv.innerHTML = `
            <div class="search-loading">
                <div class="spinner"></div>
                <p>Buscando...</p>
            </div>
        `;
        loadingDiv.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            background: var(--card-bg);
            padding: 1rem;
            border-radius: 8px;
            z-index: 1000;
            border: 1px solid var(--border-color);
        `;
        document.body.appendChild(loadingDiv);
    }
    loadingDiv.style.display = 'block';
}

function hideSearchLoading() {
    const loadingDiv = document.getElementById('searchLoading');
    if (loadingDiv) {
        loadingDiv.style.display = 'none';
    }
}

function displaySearchResults(filmes, series, query) {
    hideSearchLoading();

    const params = new URLSearchParams();
    params.append('q', query);

    window.location.href = `/movies/filmes?titulo=${encodeURIComponent(query)}`;
}

function showSearchError() {
    hideSearchLoading();
    showNotification('Erro na busca. Tente novamente.', 'error');
}

function initializeRatingStars() {
    const ratingElements = document.querySelectorAll('.rating-stars, .stars[data-rating]');

    ratingElements.forEach(element => {
        const rating = parseFloat(element.dataset.rating) || 0;
        updateStars(element, rating);
    });
}

function updateStars(element, rating) {
    const stars = Math.round(rating / 2);
    let starsHtml = '';

    for (let i = 1; i <= 5; i++) {
        if (i <= stars) {
            starsHtml += '★';
        } else {
            starsHtml += '☆';
        }
    }

    element.innerHTML = starsHtml;
}

async function playTrailer(contentId) {
    try {
        let trailerUrl = null;

        try {
            const filme = await MovieService.getFilme(contentId);
            trailerUrl = filme.urlTrailer;
        } catch {
            try {
                const serie = await MovieService.getSerie(contentId);
                trailerUrl = serie.urlTrailer;
            } catch {
                console.log('Conteúdo não encontrado');
            }
        }

        if (trailerUrl) {
            openTrailerModal(trailerUrl);
        } else {
            showNotification('Trailer não disponível', 'error');
        }
    } catch (error) {
        console.error('Erro ao buscar trailer:', error);
        showNotification('Erro ao carregar trailer', 'error');
    }
}

function openTrailerModal(trailerUrl) {
    const modal = document.createElement('div');
    modal.className = 'trailer-modal modal show';
    modal.innerHTML = `
        <div class="trailer-content">
            <button class="trailer-close" onclick="closeTrailerModal()">&times;</button>
            <div class="trailer-wrapper">
                <iframe 
                    src="${convertToEmbedUrl(trailerUrl)}" 
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

    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeTrailerModal();
        }
    });
}

function closeTrailerModal() {
    const modal = document.querySelector('.trailer-modal');
    if (modal) {
        modal.remove();
        document.body.style.overflow = '';
    }
}

function convertToEmbedUrl(url) {
    if (!url.includes('http') && !url.includes('www') && url.length >= 10 && url.length <= 15) {
        return `https://www.youtube.com/embed/${url}?autoplay=1`;
    }

    if (url.includes('youtube.com/watch')) {
        const videoId = url.split('v=')[1].split('&')[0];
        return `https://www.youtube.com/embed/${videoId}?autoplay=1`;
    }

    if (url.includes('youtu.be/')) {
        const videoId = url.split('youtu.be/')[1].split('?')[0];
        return `https://www.youtube.com/embed/${videoId}?autoplay=1`;
    }

    if (url.includes('youtube.com/embed/')) {
        return url;
    }

    return url;
}

class UIUtils {
    static showLoading(element) {
        if (element) {
            element.innerHTML = '<div class="loading">Carregando...</div>';
        }
    }

    static hideLoading(element) {
        if (element) {
            const loading = element.querySelector('.loading');
            if (loading) {
                loading.remove();
            }
        }
    }

    static showMessage(message, type = 'info') {
        showNotification(message, type);
    }

    static showError(message) {
        showNotification(message, 'error');
    }

    static showSuccess(message) {
        showNotification(message, 'success');
    }

    static formatRating(rating) {
        return parseFloat(rating).toFixed(1);
    }

    static formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('pt-BR');
    }
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 100px;
        right: 20px;
        padding: 1rem 2rem;
        border-radius: 6px;
        z-index: 3000;
        animation: slideIn 0.3s ease-out;
        color: white;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
    `;

    if (type === 'success') {
        notification.style.backgroundColor = '#4caf50';
    } else if (type === 'error') {
        notification.style.backgroundColor = '#f44336';
    } else {
        notification.style.backgroundColor = '#2196f3';
    }

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const img = entry.target;
            if (img.dataset.src) {
                img.src = img.dataset.src;
                img.classList.add('loaded');
                observer.unobserve(img);
            }
        }
    });
});

document.querySelectorAll('img[data-src]').forEach(img => {
    imageObserver.observe(img);
});

document.addEventListener('click', function(e) {
    const modal = document.getElementById('movieModal');
    if (e.target === modal) {
        modal.classList.remove('show');
        document.body.style.overflow = '';
    }
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        const modals = document.querySelectorAll('.modal.show');
        modals.forEach(modal => {
            modal.classList.remove('show');
        });

        closeTrailerModal();

        document.body.style.overflow = '';
    }
});

window.movieUtils = {
    showNotification,
    playTrailer,
    openTrailerModal,
    closeTrailerModal,
    MovieService,
    ReviewService,
    RatingService,
    UserService,
    UIUtils
};

const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .loading {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 2rem;
        color: var(--text-secondary);
    }
    
    .spinner {
        width: 20px;
        height: 20px;
        border: 2px solid var(--border-color);
        border-top-color: var(--accent-red);
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-right: 0.5rem;
    }
    
    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }
    
    .trailer-modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.95);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 3000;
    }
    
    .trailer-content {
        position: relative;
        width: 90%;
        max-width: 1000px;
        aspect-ratio: 16/9;
    }
    
    .trailer-close {
        position: absolute;
        top: -40px;
        right: 0;
        background: none;
        border: none;
        color: white;
        font-size: 2rem;
        cursor: pointer;
        transition: transform 0.3s ease;
        z-index: 1;
    }
    
    .trailer-close:hover {
        transform: rotate(90deg);
    }
    
    .trailer-wrapper {
        width: 100%;
        height: 100%;
    }
    
    .trailer-wrapper iframe {
        width: 100%;
        height: 100%;
        border-radius: 8px;
    }
    
    .search-loading {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        color: var(--text-primary);
    }
    
    .search-loading .spinner {
        width: 16px;
        height: 16px;
        margin-right: 0;
    }
`;
document.head.appendChild(style);

window.testModal = function(id, type) {
    console.log(`🧪 TESTE MANUAL: Abrindo modal ${type} ID ${id}`);
    if (typeof openMovieModal === 'function') {
        openMovieModal(id, type);
    } else {
        console.error('❌ openMovieModal não disponível');
    }
};

setTimeout(() => {
    console.log('🔍 DEBUG: Iniciando análise dos movie cards...');

    const movieCards = document.querySelectorAll('.movie-card');
    console.log(`📋 Total de movie cards encontrados: ${movieCards.length}`);

    movieCards.forEach((card, index) => {
        console.log(`\n🎬 Card ${index + 1}:`);
        console.log('  - data-movie-id:', card.getAttribute('data-movie-id'));
        console.log('  - data-show-id:', card.getAttribute('data-show-id'));
        console.log('  - data-content-type:', card.getAttribute('data-content-type'));
        console.log('  - Texto do título:', card.querySelector('.movie-name')?.textContent);
    });

    const modal = document.getElementById('movieModal');
    console.log('\n🎭 Modal movieModal existe?', modal !== null);

    console.log('\n🔧 Funções globais disponíveis:');
    console.log('  - openMovieModal:', typeof openMovieModal);
    console.log('  - MovieModal (classe):', typeof MovieModal);
    console.log('  - window.movieModal:', typeof window.movieModal);

    if (typeof MovieModal === 'function' && !window.movieModal) {
        console.log('🔧 Criando instância do MovieModal...');
        try {
            window.movieModal = new MovieModal();
            console.log('✅ MovieModal criado com sucesso');
        } catch (error) {
            console.error('❌ Erro ao criar MovieModal:', error);
        }
    }

    if (typeof openMovieModal !== 'function') {
        console.log('🔧 Criando função openMovieModal básica...');
        window.openMovieModal = function(contentId, contentType) {
            console.log(`🎬 openMovieModal chamada: ID=${contentId}, Tipo=${contentType}`);

            if (window.movieModal && typeof window.movieModal.open === 'function') {
                window.movieModal.open(contentId, contentType);
            } else {
                console.error('❌ MovieModal não disponível');
                alert(`Tentando abrir ${contentType} ID: ${contentId}`);
            }
        };
        console.log('✅ Função openMovieModal criada');
    }
}, 1000);

const debugStyle = document.createElement('style');
debugStyle.textContent = `
    .movie-card {
        border: 2px solid transparent !important;
        transition: border-color 0.3s ease !important;
    }
    .movie-card:hover {
        border-color: #ff0000 !important;
        cursor: pointer !important;
    }
`;
document.head.appendChild(debugStyle);


class FavoritoService {
    static async adicionarAosFavoritos(contentId, contentType) {
        try {
            const endpoint = `/api/favoritos/${contentType}/${contentId}`;
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const result = await response.json();

            if (result.success) {
                showNotification(result.message, 'success');
                updateFavoritoButton(contentId, contentType, true);
                return true;
            } else {
                showNotification(result.error || 'Erro ao adicionar aos favoritos', 'error');
                return false;
            }
        } catch (error) {
            console.error('Erro ao adicionar aos favoritos:', error);
            showNotification('Erro de conexão', 'error');
            return false;
        }
    }

    static async removerDosFavoritos(contentId, contentType) {
        try {
            const endpoint = `/api/favoritos/${contentType}/${contentId}`;
            const response = await fetch(endpoint, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const result = await response.json();

            if (result.success) {
                showNotification(result.message, 'success');
                updateFavoritoButton(contentId, contentType, false);
                return true;
            } else {
                showNotification(result.error || 'Erro ao remover dos favoritos', 'error');
                return false;
            }
        } catch (error) {
            console.error('Erro ao remover dos favoritos:', error);
            showNotification('Erro de conexão', 'error');
            return false;
        }
    }

    static async verificarSeFavorito(contentId, contentType) {
        try {
            const endpoint = `/api/favoritos/${contentType}/${contentId}/check`;
            const response = await fetch(endpoint);
            const result = await response.json();
            return result.isFavorito || false;
        } catch (error) {
            console.error('Erro ao verificar favorito:', error);
            return false;
        }
    }

    static async listarFavoritos() {
        try {
            const response = await fetch('/api/favoritos');
            return await response.json();
        } catch (error) {
            console.error('Erro ao listar favoritos:', error);
            return [];
        }
    }
}

function updateFavoritoButton(contentId, contentType, isFavorito) {
    const buttons = document.querySelectorAll(`[data-content-id="${contentId}"][data-content-type="${contentType}"]`);

    buttons.forEach(button => {
        if (isFavorito) {
            button.classList.add('favorito-ativo');
            button.innerHTML = `
                <svg width="20" height="20" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
                Remover dos Favoritos
            `;
            button.title = 'Remover dos favoritos';
        } else {
            button.classList.remove('favorito-ativo');
            button.innerHTML = `
                <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                </svg>
                Adicionar aos Favoritos
            `;
            button.title = 'Adicionar aos favoritos';
        }
    });
}

async function initializeFavoritoButtons() {
    if (!window.movieData?.usuarioLogado) {
        return;
    }

    const favoritoButtons = document.querySelectorAll('.btn-favorito');

    for (const button of favoritoButtons) {
        const contentId = button.getAttribute('data-content-id');
        const contentType = button.getAttribute('data-content-type');

        if (contentId && contentType) {
            const isFavorito = await FavoritoService.verificarSeFavorito(contentId, contentType);
            updateFavoritoButton(contentId, contentType, isFavorito);

            button.addEventListener('click', async function(e) {
                e.stopPropagation();
                e.preventDefault();

                const isCurrentlyFavorito = this.classList.contains('favorito-ativo');

                if (isCurrentlyFavorito) {
                    await FavoritoService.removerDosFavoritos(contentId, contentType);
                } else {
                    await FavoritoService.adicionarAosFavoritos(contentId, contentType);
                }
            });
        }
    }
}

function createFavoritoButton(contentId, contentType) {
    if (!window.movieData?.usuarioLogado) {
        return '';
    }

    return `
        <button class="btn-favorito btn-secondary" 
                data-content-id="${contentId}" 
                data-content-type="${contentType}"
                title="Adicionar aos favoritos">
            <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
            </svg>
            Adicionar aos Favoritos
        </button>
    `;
}

async function addToWatchlistDirect(movieId) {
    try {
        // Check if user is logged in
        if (!window.movieData?.usuarioLogado) {
            if (window.authFunctions?.showSignInModal) {
                window.authFunctions.showSignInModal();
            } else {
                alert('Faça login para adicionar à lista de favoritos');
            }
            return;
        }

        const contentType = 'filme'; // Por padrão, assume filme

        await FavoritoService.adicionarAosFavoritos(movieId, contentType);

    } catch (error) {
        console.error('Error adding to favorites:', error);
        showNotification('Erro de conexão', 'error');
    }
}

document.addEventListener('DOMContentLoaded', function() {
    setTimeout(initializeFavoritoButtons, 1500); // Aguarda após outros scripts
});

window.FavoritoService = FavoritoService;
window.createFavoritoButton = createFavoritoButton;

function initializeUserMenu() {
    const userMenu = document.querySelector('.user-menu');
    if (!userMenu) {
        return;
    }

    const toggleButton = userMenu.querySelector('.user-menu-toggle');
    const dropdown = userMenu.querySelector('.user-dropdown');

    toggleButton.addEventListener('click', function(event) {
        event.stopPropagation();
        userMenu.classList.toggle('open');
    });

    document.addEventListener('click', function(event) {
        if (!userMenu.contains(event.target)) {
            userMenu.classList.remove('open');
        }
    });

    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            userMenu.classList.remove('open');
        }
    });

}