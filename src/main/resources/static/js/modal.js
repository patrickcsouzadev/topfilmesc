class MovieModal {
    constructor() {
        this.modal = document.getElementById('movieModal');
        this.currentContentId = null;
        this.currentContentType = null;
        this.isLoading = false;
        this.init();
    }

    init() {
        if (!this.modal) return;

        this.modal.addEventListener('click', (e) => {
            if (e.target === this.modal) {
                this.close();
            }
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.modal.classList.contains('show')) {
                this.close();
            }
        });
    }


    async open(contentId, contentType = 'filme') {
        if (this.isLoading) return;

        console.log(`Opening modal for ${contentType} ID: ${contentId}`);

        this.currentContentId = contentId;
        this.currentContentType = contentType;
        this.isLoading = true;

        this.modal.innerHTML = '<div class="modal-content"><div class="modal-body"></div></div>';
        const modalBody = this.modal.querySelector('.modal-body');

        this.showLoading(modalBody);
        this.modal.classList.add('show');
        document.body.style.overflow = 'hidden';

        try {
            const [contentData, reviewsData] = await Promise.all([
                this.fetchContentDetails(contentId, contentType),
                this.fetchReviews(contentId, contentType)
            ]);

            console.log('Content data loaded:', contentData);
            console.log('Reviews data loaded:', reviewsData);

            this.renderContent(modalBody, contentData, reviewsData);

        } catch (error) {
            console.error(`Error loading content details for ID ${contentId}:`, error);
            this.showError(modalBody, error.message);
        } finally {
            this.isLoading = false;
        }
    }

    close() {
        this.modal.classList.remove('show');
        this.modal.innerHTML = '';
        document.body.style.overflow = '';
    }

    showLoading(modalBody) {
        modalBody.innerHTML = `
            <div class="loading-state">
                <div class="spinner"></div>
                <p>Carregando informações...</p>
            </div>
        `;
    }

    showError(modalBody, errorMessage = '') {
        modalBody.innerHTML = `
            <div class="error-state">
                <button class="modal-close">&times;</button>
                <h3>Oops! Algo deu errado</h3>
                <p>Não foi possível carregar os detalhes deste conteúdo.</p>
                ${errorMessage ? `<p><small>Erro: ${errorMessage}</small></p>` : ''}
                <button class="btn-primary" onclick="movieModal.close()">Fechar</button>
            </div>
        `;

        modalBody.querySelector('.modal-close')?.addEventListener('click', () => this.close());
    }

    async fetchContentDetails(contentId, contentType) {
        const endpoint = contentType === 'filme'
            ? `/movies/api/filmes/${contentId}`
            : `/movies/api/series/${contentId}`;

        console.log(`Fetching from: ${endpoint}`);

        const response = await fetch(endpoint);
        if (!response.ok) {
            throw new Error(`Erro ${response.status}: Conteúdo não encontrado`);
        }
        return await response.json();
    }

    async fetchReviews(contentId, contentType) {
        const endpoint = contentType === 'filme'
            ? `/reviews/api/filme/${contentId}`
            : `/reviews/api/serie/${contentId}`;

        try {
            const response = await fetch(endpoint);
            if (!response.ok) {
                console.warn(`Reviews não encontrados para ${contentType} ${contentId}`);
                return [];
            }
            return await response.json();
        } catch (error) {
            console.warn('Erro ao buscar reviews:', error);
            return [];
        }
    }

    renderContent(modalBody, content, reviews) {
        const isMovie = this.currentContentType === 'filme';
        const userIsLoggedIn = this.checkUserLoggedIn();
        const userIsAdmin = this.checkUserIsAdmin();

        const safeUserIsAdmin = userIsLoggedIn && userIsAdmin;

        console.log('Render content - User status:', {
            userIsLoggedIn: userIsLoggedIn,
            userIsAdmin: userIsAdmin,
            safeUserIsAdmin: safeUserIsAdmin
        });

        const title = content.titulo || 'Título não disponível';
        const description = content.descricao || 'Descrição não disponível';
        const year = content.anoLancamento || 'N/A';
        const rating = content.mediaRating || 0;
        const duration = isMovie
            ? (content.duracao || content.duracaoMinutos ? `${content.duracaoMinutos || 'N/A'} min` : 'N/A')
            : (content.numeroTemporadas ? `${content.numeroTemporadas} temporadas` : 'N/A');
        const director = isMovie ? (content.diretor || 'N/A') : (content.criador || 'N/A');
        const cast = content.elenco || 'N/A';
        const posterUrl = content.urlPoster || '/images/no-poster.jpg';
        const backgroundUrl = content.urlBackgroundImage || content.urlPoster || '/images/default-bg.jpg';
        const trailerUrl = content.urlTrailer || '';

        modalBody.innerHTML = `
            <button class="modal-close">&times;</button>
            
            ${safeUserIsAdmin ? `
                <div class="admin-controls">
                    <span class="admin-badge">🛡️ Admin</span>
                    <button class="btn-delete-content" data-content-id="${this.currentContentId}" data-content-type="${this.currentContentType}">
                        🗑️ Deletar ${isMovie ? 'Filme' : 'Série'}
                    </button>
                </div>
            ` : ''}
            
            <div class="movie-details-header" style="background-image: linear-gradient(rgba(0,0,0,0.3), #141414), url('${backgroundUrl}')">
            </div>
            
            <div class="movie-details-content">
                <h2 class="movie-details-title">${title}</h2>
                
                <div class="movie-details-meta">
                    <div class="rating">
                        <span class="stars">${this.getStarRating(rating)}</span>
                        <span class="score">${rating.toFixed(1)}</span>
                    </div>
                    <span class="year">${year}</span>
                    <span class="duration">${duration}</span>
                </div>
                
                <p class="movie-details-description">${description}</p>
                
                <div class="movie-details-info">
                    <p><strong>${isMovie ? 'Diretor:' : 'Criador:'}</strong> ${director}</p>
                    <p><strong>Elenco:</strong> ${cast}</p>
                </div>
                
<div class="modal-actions-container">
    ${trailerUrl ? `
        <button class="btn-action btn-action-primary watch-trailer" data-trailer="${trailerUrl}">
            <svg width="20" height="20" fill="currentColor" viewBox="0 0 24 24"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
            <span>Assistir Trailer</span>
        </button>
    ` : ''}
    ${userIsLoggedIn ? `
        <button class="btn-action btn-action-secondary add-to-favorites" data-content-id="${content.id}" data-content-type="${this.currentContentType}">
            <svg class="icon-heart" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
            <span>Favoritar</span>
        </button>
    ` : ''}
</div>
            
            <div class="reviews-section">
                <div class="reviews-header">
                    <h3 class="reviews-title">Reviews & Comentários</h3>
                    ${this.renderReviewActions(userIsLoggedIn)}
                </div>
                
                ${userIsLoggedIn ? this.renderReviewForm() : ''}
                
                <div class="reviews-list">
                    ${this.renderReviews(reviews, safeUserIsAdmin)}
                </div>
            </div>
        `;

        this.attachEventListeners(modalBody);
    }

    renderReviewActions(userIsLoggedIn) {
        if (userIsLoggedIn) {
            return '<button class="write-review-btn">Escrever Review</button>';
        } else {
            return `
                <div class="login-prompt">
                    <p style="color: var(--text-secondary); font-style: italic;">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" style="vertical-align: text-bottom; margin-right: 4px;">
                            <path d="M9 12l2 2 4-4"></path>
                            <path d="M21 12c-1 0-3-1-3-3s2-3 3-3 3 1 3 3-2 3-3 3"></path>
                            <path d="M3 12c1 0 3-1 3-3s-2-3-3-3-3 1-3 3 2 3 3 3"></path>
                        </svg>
                        Faça login para deixar um review
                    </p>
                    <button class="btn-secondary" onclick="document.querySelector('.sign-in-btn')?.click() || window.authFunctions?.showSignInModal()">
                        Fazer Login
                    </button>
                </div>
            `;
        }
    }

    renderReviewForm() {
        return `
            <div id="reviewForm" class="review-form" style="display: none;">
                <form>
                    <div class="form-group">
                        <label class="form-label">Sua Avaliação (Estrelas)</label>
                        <div class="star-rating">
                            ${[5,4,3,2,1].map(i => `
                                <input type="radio" id="star${i}" name="rating" value="${i}" required>
                                <label for="star${i}">★</label>
                            `).join('')}
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Seu Comentário</label>
                        <textarea class="form-textarea" name="comentario" required minlength="5" 
                                  placeholder="Compartilhe sua opinião sobre este ${this.currentContentType}..."></textarea>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn-primary">Postar Review</button>
                        <button type="button" class="btn-secondary cancel-review">Cancelar</button>
                    </div>
                </form>
            </div>
        `;
    }

    checkUserLoggedIn() {
        try {
            const userMenuExists = !!(document.querySelector('.user-menu') ||
                document.querySelector('.user-menu-container'));
            const logoutExists = !!(document.querySelector('a[href*="logout"]') ||
                document.querySelector('.logout'));
            const thymeleafUserData = !!(window.movieDetailData?.usuarioLogado ||
                window.movieData?.usuarioLogado);
            const dataUserLogged = document.body.getAttribute('data-user-logged') === 'true';
            const noSignInButton = !document.querySelector('.sign-in-btn');

            const isLoggedIn = userMenuExists || logoutExists || thymeleafUserData ||
                dataUserLogged || noSignInButton;

            return isLoggedIn;

        } catch (error) {
            console.warn('Erro na verificação de login:', error);
            return false;
        }
    }

    checkUserIsAdmin() {
        try {
            const userIsLoggedIn = this.checkUserLoggedIn();
            if (!userIsLoggedIn) {
                console.log('❌ Usuário não logado, não pode ser admin');
                return false;
            }

            const jsAdmin = window.movieData?.usuarioLogado?.admin || window.movieDetailData?.usuarioLogado?.admin;
            const dataAdmin = document.body.getAttribute('data-user-admin') === 'true';
            const roleAdmin = (window.movieData?.usuarioLogado?.role || window.movieDetailData?.usuarioLogado?.role) === 'ROLE_ADMIN';

            const isAdmin = jsAdmin === true || dataAdmin === true || roleAdmin === true;

            console.log('🔍 Verificação de admin:', {
                jsAdmin: jsAdmin,
                dataAdmin: dataAdmin,
                roleAdmin: roleAdmin,
                resultado: isAdmin
            });

            return isAdmin;

        } catch (error) {
            console.warn('❌ Erro na verificação de admin:', error);
            return false;
        }
    }

    getCurrentUserId() {
        try {
            let userId = window.movieData?.usuarioLogado?.id;
            if (userId !== null && userId !== undefined) {
                console.log('✅ ID obtido de window.movieData:', userId, typeof userId);
                return userId;
            }

            userId = window.movieDetailData?.usuarioLogado?.id;
            if (userId !== null && userId !== undefined) {
                console.log('✅ ID obtido de window.movieDetailData:', userId, typeof userId);
                return userId;
            }

            const dataUserId = document.body.getAttribute('data-user-id');
            if (dataUserId && dataUserId !== '' && dataUserId !== 'null') {
                const parsedId = isNaN(dataUserId) ? dataUserId : parseInt(dataUserId);
                console.log('✅ ID obtido de data-user-id:', parsedId, typeof parsedId);
                return parsedId;
            }

            console.warn('❌ ID do usuário não encontrado em nenhuma fonte');
            return null;

        } catch (error) {
            console.error('❌ Erro ao obter ID do usuário:', error);
            return null;
        }
    }

    renderReviews(reviews, userIsAdmin) {
        if (!reviews || reviews.length === 0) {
            return `
            <div class="no-reviews">
                <p>Ainda não há reviews para este conteúdo.</p>
                <p><small>Seja o primeiro a comentar!</small></p>
            </div>
        `;
        }

        const userIsLoggedIn = this.checkUserLoggedIn();
        const currentUserId = this.getCurrentUserId();

        console.log('🔍 DEBUG renderReviews:', {
            userIsLoggedIn: userIsLoggedIn,
            currentUserId: currentUserId,
            currentUserIdType: typeof currentUserId,
            userIsAdmin: userIsAdmin,
            reviewsCount: reviews.length
        });

        return reviews.map((review, index) => {
            const reviewUserId = review.usuario?.id;

            console.log(`📝 Review ${index}:`, {
                reviewId: review.id,
                reviewUserId: reviewUserId,
                reviewUserIdType: typeof reviewUserId,
                reviewAuthor: review.usuario?.nomeCompleto,
                currentUserId: currentUserId
            });

            let isOwnReview = false;

            if (userIsLoggedIn && currentUserId !== null && currentUserId !== undefined &&
                reviewUserId !== null && reviewUserId !== undefined) {

                isOwnReview = (reviewUserId === currentUserId) ||
                    (String(reviewUserId) === String(currentUserId)) ||
                    (Number(reviewUserId) === Number(currentUserId));
            }

            const canDelete = userIsAdmin || isOwnReview;

            console.log(`✅ Review ${index} decisão:`, {
                isOwnReview: isOwnReview,
                canDelete: canDelete,
                reason: canDelete ? (userIsAdmin ? 'admin' : 'own review') : 'no permission'
            });

            return `
            <div class="review-item" data-review-id="${review.id}">
                <div class="review-header">
                    <div class="review-author-info">
                        <span class="review-author">${review.usuario?.nomeCompleto || 'Usuário Anônimo'}</span>
                        <span class="review-rating">${'★'.repeat(review.rating || 0)}${'☆'.repeat(5 - (review.rating || 0))}</span>
                        ${isOwnReview ? '<span class="own-review-badge">Seu review</span>' : ''}
                    </div>
                    ${canDelete ? `
                        <button class="btn-delete-review" 
                                data-review-id="${review.id}" 
                                data-is-own="${isOwnReview}"
                                title="${isOwnReview ? 'Deletar seu review' : 'Deletar review (Admin)'}">
                            🗑️
                        </button>
                    ` : ''}
                </div>
                <p class="review-content">${review.comentario || ''}</p>
                <span class="review-date">${review.dataCriacao ? new Date(review.dataCriacao).toLocaleDateString('pt-BR') : ''}</span>
            </div>
        `;
        }).join('');
    }

    attachEventListeners(modalBody) {
        modalBody.querySelector('.modal-close')?.addEventListener('click', () => this.close());

        modalBody.querySelector('.write-review-btn')?.addEventListener('click', () => {
            const reviewForm = modalBody.querySelector('#reviewForm');
            if (reviewForm) {
                reviewForm.style.display = 'block';
                reviewForm.scrollIntoView({ behavior: 'smooth' });
            }
        });

        modalBody.querySelector('.cancel-review')?.addEventListener('click', () => {
            const reviewForm = modalBody.querySelector('#reviewForm');
            if (reviewForm) {
                reviewForm.style.display = 'none';
                reviewForm.querySelector('form')?.reset();
            }
        });

        modalBody.querySelector('#reviewForm form')?.addEventListener('submit', (e) => this.submitReview(e));

        modalBody.querySelector('.watch-trailer')?.addEventListener('click', (e) => {
            const trailerId = e.currentTarget.getAttribute('data-trailer');
            this.playTrailer(trailerId);
        });

        modalBody.querySelector('.btn-delete-content')?.addEventListener('click', (e) => {
            // VERIFICAÇÃO DE SEGURANÇA: Confirma que é admin antes de executar
            if (!this.checkUserLoggedIn() || !this.checkUserIsAdmin()) {
                alert('❌ Acesso negado! Apenas administradores podem deletar conteúdo.');
                console.warn('🚨 Tentativa de deleção não autorizada');
                return;
            }

            const contentId = e.currentTarget.getAttribute('data-content-id');
            const contentType = e.currentTarget.getAttribute('data-content-type');
            this.deleteContent(contentId, contentType);
        });

        modalBody.querySelectorAll('.btn-delete-review').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const reviewId = e.currentTarget.getAttribute('data-review-id');
                const isOwnReview = e.currentTarget.getAttribute('data-is-own') === 'true';
                const userIsAdmin = this.checkUserIsAdmin();
                const userIsLoggedIn = this.checkUserLoggedIn();

                console.log('🗑️ Tentativa de deletar review:', {
                    reviewId,
                    isOwnReview,
                    userIsAdmin,
                    userIsLoggedIn,
                    currentUserId: window.movieData?.usuarioLogado?.id
                });

                if (!userIsLoggedIn) {
                    alert('❌ Você precisa estar logado para deletar reviews.');
                    return;
                }

                if (!userIsAdmin && !isOwnReview) {
                    alert('❌ Você só pode deletar suas próprias reviews.');
                    console.warn('🚨 Tentativa de deletar review de outro usuário');
                    return;
                }

                console.log('✅ Permissão concedida para deletar review');

                this.deleteReview(reviewId, isOwnReview);
            });
        });

        modalBody.querySelector('.add-to-favorites')?.addEventListener('click', (e) => {
            const button = e.currentTarget;
            const contentId = button.dataset.contentId;
            const contentType = button.dataset.contentType;
            this.toggleFavorite(contentId, contentType, button);
        });

        const favButton = modalBody.querySelector('.add-to-favorites');
        if (favButton) {
            this.checkFavoriteStatus(favButton.dataset.contentId, favButton.dataset.contentType, favButton);
        }
    }

    async toggleFavorite(contentId, contentType, button) {
        const isFavorito = button.classList.contains('favorito-ativo');
        const endpoint = `/api/favoritos/${contentType}/${contentId}`;
        const method = isFavorito ? 'DELETE' : 'POST';

        try {
            const response = await fetch(endpoint, { method: method });
            const result = await response.json();

            if (result.success) {
                this.updateFavoriteButton(!isFavorito, button);
                //showNotification(result.message, 'success');
            } else {
                alert(`Erro: ${result.error}`);
            }
        } catch (error) {
            console.error('Erro ao favoritar:', error);
            alert('Erro de conexão ao favoritar.');
        }
    }

    async checkFavoriteStatus(contentId, contentType, button) {
        try {
            const endpoint = `/api/favoritos/${contentType}/${contentId}/check`;
            const response = await fetch(endpoint);
            const result = await response.json();
            if (result.isFavorito) {
                this.updateFavoriteButton(true, button);
            }
        } catch (error) {
            console.warn('Não foi possível verificar status de favorito (usuário pode não estar logado).');
        }
    }

    updateFavoriteButton(isFavorito, button) {
        const span = button.querySelector('span');
        const svgIcon = button.querySelector('.icon-heart');

        if (isFavorito) {
            button.classList.add('favorito-ativo');
            if (span) span.textContent = 'Favoritado';
        } else {
            button.classList.remove('favorito-ativo');
            if (span) span.textContent = 'Favoritar';
        }
    }

    async deleteContent(contentId, contentType) {
        const contentName = contentType === 'filme' ? 'filme' : 'série';

        const confirmMessage = `⚠️ ATENÇÃO!\n\nVocê tem certeza que deseja deletar este ${contentName}?\n\nEsta ação:\n• NÃO pode ser desfeita\n• Irá deletar TODOS os reviews associados\n• Irá remover o conteúdo permanentemente\n\nDigite "DELETAR" para confirmar:`;

        const confirmation = prompt(confirmMessage);

        if (confirmation !== 'DELETAR') {
            alert('Deleção cancelada. O conteúdo não foi deletado.');
            return;
        }

        try {
            const endpoint = `/admin/api/${contentType === 'filme' ? 'filmes' : 'series'}/${contentId}`;

            const response = await fetch(endpoint, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const result = await response.json();

            if (result.success) {
                alert(`✅ ${contentName.charAt(0).toUpperCase() + contentName.slice(1)} deletado com sucesso!`);
                this.close();
                window.location.reload();
            } else {
                alert(`❌ Erro ao deletar ${contentName}: ${result.error}`);
            }
        } catch (error) {
            console.error('Erro ao deletar conteúdo:', error);
            alert(`❌ Erro de conexão ao deletar ${contentName}.`);
        }
    }

    async deleteReview(reviewId, isOwnReview = false) {
        const confirmMessage = isOwnReview
            ? '⚠️ Tem certeza que deseja deletar seu review?\n\nEsta ação não pode ser desfeita.'
            : '⚠️ Tem certeza que deseja deletar este review?\n\nEsta ação não pode ser desfeita.';

        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            const endpoint = isOwnReview
                ? `/reviews/api/${reviewId}`
                : `/admin/api/reviews/${reviewId}`;

            console.log(`🗑️ Deletando review ${reviewId} via ${endpoint} (própria: ${isOwnReview})`);

            const response = await fetch(endpoint, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            let result;
            try {
                result = await response.json();
            } catch (e) {
                result = response.ok ? { message: 'Review deletado' } : { error: 'Erro desconhecido' };
            }

            console.log('Resposta do servidor:', result);

            const isSuccess = response.ok && (result.success || result.message);

            if (isSuccess) {
                const successMessage = isOwnReview
                    ? '✅ Seu review foi deletado com sucesso!'
                    : '✅ Review deletado com sucesso!';
                alert(successMessage);

                const reviewElement = document.querySelector(`[data-review-id="${reviewId}"]`);
                if (reviewElement) {
                    reviewElement.style.animation = 'fadeOut 0.3s ease-out';
                    setTimeout(() => {
                        reviewElement.remove();
                        const reviewsList = document.querySelector('.reviews-list');
                        if (reviewsList && reviewsList.children.length === 0) {
                            reviewsList.innerHTML = `
                                <div class="no-reviews">
                                    <p>Ainda não há reviews para este conteúdo.</p>
                                    <p><small>Seja o primeiro a comentar!</small></p>
                                </div>
                            `;
                        }
                    }, 300);
                }
            } else {
                const errorMessage = result.error || result.message || 'Erro desconhecido';
                console.error('Erro ao deletar review:', errorMessage);
                alert(`❌ Erro ao deletar review: ${errorMessage}`);
            }
        } catch (error) {
            console.error('Erro de conexão ao deletar review:', error);
            alert('❌ Erro de conexão ao deletar review.');
        }
    }

    async submitReview(event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);
        const reviewData = {
            rating: parseInt(formData.get('rating')),
            comentario: formData.get('comentario')
        };

        if (!reviewData.rating) {
            alert('Por favor, selecione uma nota em estrelas.');
            return;
        }

        if (!reviewData.comentario || reviewData.comentario.trim().length < 5) {
            alert('Por favor, escreva um comentário com pelo menos 5 caracteres.');
            return;
        }

        const endpoint = this.currentContentType === 'filme'
            ? `/reviews/api/filme/${this.currentContentId}`
            : `/reviews/api/serie/${this.currentContentId}`;

        const submitBtn = form.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.textContent = 'Enviando...';
        submitBtn.disabled = true;

        try {
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(reviewData)
            });

            if (response.ok) {
                alert('Review publicado com sucesso!');
                this.open(this.currentContentId, this.currentContentType);
            } else {
                const errorData = await response.json().catch(() => ({}));
                alert(`Erro ao postar review: ${errorData.error || 'Tente novamente.'}`);
            }
        } catch (error) {
            console.error('Failed to submit review:', error);
            alert('Erro de conexão ao postar review.');
        } finally {
            submitBtn.textContent = originalText;
            submitBtn.disabled = false;
        }
    }

    playTrailer(youtubeId) {
        if (!youtubeId) {
            alert('Trailer não disponível para este conteúdo.');
            return;
        }

        const oldTrailerModal = document.querySelector('.trailer-overlay');
        if (oldTrailerModal) {
            oldTrailerModal.remove();
        }

        const embedUrl = `https://www.youtube.com/embed/${youtubeId}?autoplay=1&rel=0`;

        const trailerModal = document.createElement('div');
        trailerModal.className = 'trailer-overlay';
        trailerModal.innerHTML = `
            <div class="trailer-modal-content">
                <button class="trailer-modal-close">×</button>
                <div class="trailer-iframe-wrapper">
                    <iframe src="${embedUrl}" 
                            frameborder="0" 
                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                            allowfullscreen>
                    </iframe>
                </div>
            </div>
        `;

        document.body.appendChild(trailerModal);
        document.body.style.overflow = 'hidden';

        trailerModal.querySelector('.trailer-modal-close').addEventListener('click', () => {
            document.body.removeChild(trailerModal);
            document.body.style.overflow = '';
        });

        trailerModal.addEventListener('click', (e) => {
            if (e.target === trailerModal) {
                document.body.removeChild(trailerModal);
                document.body.style.overflow = '';
            }
        });

        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                if (document.body.contains(trailerModal)) {
                    document.body.removeChild(trailerModal);
                    document.body.style.overflow = '';
                }
                document.removeEventListener('keydown', handleEscape);
            }
        };
        document.addEventListener('keydown', handleEscape);
    }

    getStarRating(rating) {
        const fullStars = Math.round(rating || 0);
        const maxStars = 5;
        return '★'.repeat(Math.min(fullStars, maxStars)) + '☆'.repeat(Math.max(0, maxStars - fullStars));
    }
}

let movieModal;
document.addEventListener('DOMContentLoaded', () => {
    movieModal = new MovieModal();
    console.log('MovieModal initialized');
});

function openMovieModal(contentId, contentType) {
    if (movieModal) {
        movieModal.open(contentId, contentType);
    } else {
        console.error('MovieModal not initialized');
    }
}