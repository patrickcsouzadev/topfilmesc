document.addEventListener('DOMContentLoaded', function() {
    initializeMovieDetail();
});

function initializeMovieDetail() {
    initializeTrailerPlayer();
    initializeRatingSystem();
    initializeReviewForm();
    initializeReviewInteractions();
    loadUserRating();
    setupImageGallery();
}

function initializeTrailerPlayer() {
    const trailerBtn = document.querySelector('.watch-trailer');

    trailerBtn?.addEventListener('click', function() {
        const trailerUrl = this.getAttribute('data-trailer') ||
            window.movieDetailData?.content?.urlTrailer;

        if (trailerUrl) {
            openTrailerModal(trailerUrl);
        } else {
            showNotification('Trailer não disponível', 'error');
        }
    });
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
                    title="Movie Trailer"
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

    const handleEscape = (e) => {
        if (e.key === 'Escape') {
            closeTrailerModal();
            document.removeEventListener('keydown', handleEscape);
        }
    };
    document.addEventListener('keydown', handleEscape);
}

function closeTrailerModal() {
    const modal = document.querySelector('.trailer-modal');
    if (modal) {
        modal.remove();
        document.body.style.overflow = '';
    }
}

function convertToEmbedUrl(url) {
    const youtubeIdPattern = /^[A-Za-z0-9_-]{10,15}$/;
    if (youtubeIdPattern.test(url)) {
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

function initializeRatingSystem() {
    if (!window.movieDetailData?.usuarioLogado) return;

    const stars = document.querySelectorAll('#userRating .star');
    const submitBtn = document.getElementById('submitRating');
    let selectedRating = 0;
    let currentUserRating = 0;

    stars.forEach((star, index) => {
        const rating = index + 1;

        star.addEventListener('mouseenter', () => {
            highlightStars(rating);
        });

        star.addEventListener('click', () => {
            selectedRating = rating;
            highlightStars(selectedRating);
            updateSubmitButton();
        });
    });

    const ratingContainer = document.getElementById('userRating');
    ratingContainer?.addEventListener('mouseleave', () => {
        highlightStars(selectedRating || currentUserRating);
    });

    submitBtn?.addEventListener('click', async () => {
        if (selectedRating === 0) {
            showNotification('Selecione uma nota de 1 a 10', 'error');
            return;
        }

        await submitUserRating(selectedRating);
    });

    function updateSubmitButton() {
        if (submitBtn) {
            if (selectedRating > 0) {
                submitBtn.textContent = currentUserRating > 0 ? 'Atualizar Nota' : 'Avaliar';
                submitBtn.disabled = false;
            } else {
                submitBtn.textContent = 'Avaliar';
                submitBtn.disabled = true;
            }
        }
    }
}

function highlightStars(rating) {
    const stars = document.querySelectorAll('#userRating .star');
    stars.forEach((star, index) => {
        if (index < rating) {
            star.style.color = '#ffd700';
            star.style.transform = 'scale(1.1)';
        } else {
            star.style.color = 'var(--text-secondary)';
            star.style.transform = 'scale(1)';
        }
    });
}

async function loadUserRating() {
    if (!window.movieDetailData?.usuarioLogado) return;

    try {
        const contentId = window.movieDetailData.content.id;
        const isMovie = window.movieDetailData.isMovie;
        const endpoint = isMovie ?
            `/ratings/api/filme/${contentId}` :
            `/ratings/api/serie/${contentId}`;

        const response = await fetch(`${endpoint}/user/${window.movieDetailData.usuarioId}`);

        if (response.ok) {
            const userRating = await response.json();
            if (userRating && userRating.nota) {
                const rating = Math.round(userRating.nota);
                highlightStars(rating);

                // Update submit button
                const submitBtn = document.getElementById('submitRating');
                if (submitBtn) {
                    submitBtn.textContent = 'Atualizar Nota';
                }
            }
        }
    } catch (error) {
        console.error('Error loading user rating:', error);
    }
}

async function submitUserRating(rating) {
    const submitBtn = document.getElementById('submitRating');
    const originalText = submitBtn.textContent;

    submitBtn.textContent = 'Enviando...';
    submitBtn.disabled = true;

    try {
        const contentId = window.movieDetailData.content.id;
        const isMovie = window.movieDetailData.isMovie;
        const endpoint = isMovie ?
            `/ratings/api/filme/${contentId}` :
            `/ratings/api/serie/${contentId}`;

        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nota: rating,
                usuarioId: window.movieDetailData.usuarioId
            })
        });

        if (response.ok) {
            showNotification('Avaliação enviada com sucesso!', 'success');

            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            const errorText = await response.text();
            showNotification(errorText || 'Erro ao enviar avaliação', 'error');
        }
    } catch (error) {
        console.error('Error submitting rating:', error);
        showNotification('Erro de conexão. Tente novamente.', 'error');
    } finally {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

function initializeReviewForm() {
    const writeReviewBtn = document.querySelector('.write-review-btn');
    const reviewForm = document.getElementById('reviewForm');
    const form = reviewForm?.querySelector('form');

    writeReviewBtn?.addEventListener('click', function() {
        // Check if user is logged in
        if (!window.movieDetailData?.usuarioLogado) {
            if (window.authFunctions) {
                window.authFunctions.showSignInModal();
            }
            return;
        }

        toggleReviewForm();
    });

    form?.addEventListener('submit', async function(e) {
        e.preventDefault();
        await submitReview(e);
    });
}

function toggleReviewForm() {
    const form = document.getElementById('reviewForm');
    if (form) {
        form.classList.toggle('show');

        if (form.classList.contains('show')) {
            const textarea = form.querySelector('textarea');
            setTimeout(() => textarea?.focus(), 100);
        }
    }
}

async function submitReview(event) {
    const form = event.target;
    const formData = new FormData(form);
    const comment = formData.get('comentario');

    if (!comment || comment.trim().length < 10) {
        showNotification('O comentário deve ter pelo menos 10 caracteres', 'error');
        return;
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;

    submitBtn.textContent = 'Publicando...';
    submitBtn.disabled = true;

    try {
        const contentId = window.movieDetailData.content.id;
        const isMovie = window.movieDetailData.isMovie;
        const endpoint = isMovie ?
            `/reviews/api/filme/${contentId}` :
            `/reviews/api/serie/${contentId}`;

        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                comentario: comment,
                usuarioId: window.movieDetailData.usuarioId
            })
        });

        if (response.ok) {
            const review = await response.json();
            addReviewToList(review);
            form.reset();
            toggleReviewForm();
            showNotification('Review publicado com sucesso!', 'success');
        } else {
            const errorText = await response.text();
            showNotification(errorText || 'Erro ao publicar review', 'error');
        }
    } catch (error) {
        console.error('Error submitting review:', error);
        showNotification('Erro de conexão. Tente novamente.', 'error');
    } finally {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

function initializeReviewInteractions() {
    if (window.movieDetailData?.usuarioLogado) {
        document.addEventListener('click', function(e) {
            if (e.target.closest('.review-like')) {
                handleReviewLike(e.target.closest('.review-like'));
            }
        });
    }
}

async function handleReviewLike(button) {
    const reviewId = button.dataset.reviewId;
    const isLiked = button.classList.contains('liked');

    try {
        const endpoint = `/reviews/api/${reviewId}/like`;
        const response = await fetch(endpoint, {
            method: isLiked ? 'DELETE' : 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                usuarioId: window.movieDetailData.usuarioId
            })
        });

        if (response.ok) {
            button.classList.toggle('liked');
            const count = button.querySelector('.like-count');
            const currentCount = parseInt(count?.textContent) || 0;
            if (count) {
                count.textContent = isLiked ? currentCount - 1 : currentCount + 1;
            }

            button.style.transform = 'scale(1.1)';
            setTimeout(() => {
                button.style.transform = 'scale(1)';
            }, 150);
        }
    } catch (error) {
        console.error('Error liking review:', error);
        showNotification('Erro ao curtir review', 'error');
    }
}

function setupImageGallery() {
    const posterImage = document.querySelector('.movie-poster-detail img');

    posterImage?.addEventListener('click', function() {
        openImageViewer(this.src, this.alt);
    });
}

function openImageViewer(imageSrc, altText) {
    const viewer = document.createElement('div');
    viewer.className = 'image-viewer';
    viewer.innerHTML = `
        <div class="viewer-overlay"></div>
        <div class="viewer-content">
            <button class="viewer-close" onclick="closeImageViewer()">&times;</button>
            <img src="${imageSrc}" alt="${altText}">
            <div class="viewer-info">
                <h3>${altText}</h3>
                <button class="viewer-download" onclick="downloadImage('${imageSrc}', '${altText}')">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                        <polyline points="7 10 12 15 17 10"></polyline>
                        <line x1="12" y1="15" x2="12" y2="3"></line>
                    </svg>
                    Download
                </button>
            </div>
        </div>
    `;

    document.body.appendChild(viewer);
    document.body.style.overflow = 'hidden';

    viewer.addEventListener('click', function(e) {
        if (e.target === viewer || e.target.classList.contains('viewer-overlay')) {
            closeImageViewer();
        }
    });

    const handleEscape = (e) => {
        if (e.key === 'Escape') {
            closeImageViewer();
            document.removeEventListener('keydown', handleEscape);
        }
    };
    document.addEventListener('keydown', handleEscape);

    setTimeout(() => {
        viewer.classList.add('show');
    }, 10);
}

function closeImageViewer() {
    const viewer = document.querySelector('.image-viewer');
    if (viewer) {
        viewer.classList.add('closing');
        setTimeout(() => {
            viewer.remove();
            document.body.style.overflow = '';
        }, 300);
    }
}

function downloadImage(imageSrc, filename) {
    const link = document.createElement('a');
    link.href = imageSrc;
    link.download = filename.replace(/[^a-zA-Z0-9]/g, '_') + '.jpg';
    link.click();
}

function addReviewToList(review) {
    const reviewsList = document.querySelector('.reviews-list');
    const noReviews = document.querySelector('.no-reviews');

    if (noReviews) {
        noReviews.remove();
    }

    const reviewDate = new Date(review.dataCriacao);
    const formattedDate = reviewDate.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });

    const reviewHTML = `
        <div class="review-item" data-review-id="${review.id}">
            <div class="review-header">
                <div>
                    <span class="review-author">${review.usuario.nomeCompleto}</span>
                    <span class="review-date">${formattedDate}</span>
                </div>
            </div>
            <p class="review-content">${review.comentario}</p>
            ${window.movieDetailData?.usuarioLogado ? `
                <div class="review-actions">
                    <button class="review-like" data-review-id="${review.id}">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
                        </svg>
                        <span class="like-count">0</span>
                    </button>
                </div>
            ` : ''}
        </div>
    `;

    reviewsList?.insertAdjacentHTML('afterbegin', reviewHTML);

    const newReview = reviewsList?.querySelector('.review-item');
    if (newReview) {
        newReview.style.opacity = '0';
        newReview.style.transform = 'translateY(-20px)';
        setTimeout(() => {
            newReview.style.transition = 'all 0.3s ease';
            newReview.style.opacity = '1';
            newReview.style.transform = 'translateY(0)';
        }, 100);
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
        border-radius: 8px;
        z-index: 3000;
        animation: slideIn 0.3s ease-out;
        color: white;
        font-weight: 500;
        box-shadow: 0 8px 25px rgba(0,0,0,0.3);
        max-width: 300px;
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
    }, 4000);
}

const style = document.createElement('style');
style.textContent = `
    .image-viewer {
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
        opacity: 0;
        transition: opacity 0.3s ease;
    }
    
    .image-viewer.show {
        opacity: 1;
    }
    
    .image-viewer.closing {
        opacity: 0;
    }
    
    .viewer-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
    }
    
    .viewer-content {
        position: relative;
        max-width: 90%;
        max-height: 90%;
        display: flex;
        flex-direction: column;
        align-items: center;
    }
    
    .viewer-content img {
        max-width: 100%;
        max-height: 80vh;
        border-radius: 12px;
        box-shadow: 0 20px 60px rgba(0,0,0,0.5);
    }
    
    .viewer-close {
        position: absolute;
        top: -40px;
        right: 0;
        background: rgba(0,0,0,0.7);
        border: none;
        color: white;
        font-size: 2rem;
        cursor: pointer;
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
    }
    
    .viewer-close:hover {
        background: var(--accent-red);
        transform: rotate(90deg);
    }
    
    .viewer-info {
        margin-top: 1rem;
        text-align: center;
        color: white;
    }
    
    .viewer-info h3 {
        margin: 0 0 1rem 0;
        font-size: 1.2rem;
    }
    
    .viewer-download {
        background: var(--accent-red);
        color: white;
        border: none;
        padding: 0.5rem 1rem;
        border-radius: 6px;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        transition: all 0.3s ease;
    }
    
    .viewer-download:hover {
        background: var(--accent-red-hover);
        transform: translateY(-2px);
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
        max-width: 1200px;
        aspect-ratio: 16/9;
    }
    
    .trailer-close {
        position: absolute;
        top: -50px;
        right: 0;
        background: rgba(0,0,0,0.7);
        border: none;
        color: white;
        font-size: 2rem;
        cursor: pointer;
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        z-index: 1;
    }
    
    .trailer-close:hover {
        background: var(--accent-red);
        transform: rotate(90deg);
    }
    
    .trailer-wrapper {
        width: 100%;
        height: 100%;
        border-radius: 12px;
        overflow: hidden;
        box-shadow: 0 20px 60px rgba(0,0,0,0.5);
    }
    
    .trailer-wrapper iframe {
        width: 100%;
        height: 100%;
        border: none;
    }
    
    #userRating .star {
        transition: all 0.2s ease;
        cursor: pointer;
        font-size: 1.8rem;
        padding: 0.2rem;
    }
    
    #userRating .star:hover {
        transform: scale(1.2);
    }
    
    .review-item {
        transition: all 0.3s ease;
    }
    
    .review-like {
        transition: all 0.2s ease;
    }
    
    .review-like:hover {
        transform: translateY(-1px);
    }
    
    .review-like.liked {
        animation: likeAnimation 0.3s ease;
    }
    
    @keyframes likeAnimation {
        0% { transform: scale(1); }
        50% { transform: scale(1.2); }
        100% { transform: scale(1); }
    }
    
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
    
    @media (max-width: 768px) {
        .trailer-content {
            width: 95%;
        }
        
        .trailer-close {
            top: -40px;
            font-size: 1.5rem;
            width: 35px;
            height: 35px;
        }
        
        .viewer-content {
            max-width: 95%;
        }
        
        #userRating .star {
            font-size: 1.5rem;
            padding: 0.1rem;
        }
    }
`;
document.head.appendChild(style);

window.movieDetailFunctions = {
    openTrailerModal,
    closeTrailerModal,
    toggleReviewForm,
    submitUserRating,
    showNotification,
    openImageViewer,
    closeImageViewer
};