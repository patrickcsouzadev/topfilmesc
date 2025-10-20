document.addEventListener('DOMContentLoaded', function() {
    initializeAuth();
});

function initializeAuth() {
    const signUpBtn = document.querySelector('.sign-up');
    const signInBtn = document.querySelector('.sign-in-btn');

    signUpBtn?.addEventListener('click', showSignUpModal);
    signInBtn?.addEventListener('click', showSignInModal);

    const signUpForm = document.getElementById('signUpForm');
    const signInForm = document.getElementById('signInForm');

    signUpForm?.addEventListener('submit', handleSignUp);
    signInForm?.addEventListener('submit', handleSignIn);

    const passwordInput = document.getElementById('signupPassword');
    passwordInput?.addEventListener('input', checkPasswordStrength);

    const confirmPasswordInput = document.getElementById('signupConfirmPassword');
    confirmPasswordInput?.addEventListener('input', validatePasswordMatch);

    const emailInputs = document.querySelectorAll('input[type="email"]');
    emailInputs.forEach(input => {
        input.addEventListener('blur', validateEmail);
    });

    const socialButtons = document.querySelectorAll('.social-btn');
    socialButtons.forEach(btn => {
        btn.addEventListener('click', handleSocialAuth);
    });

    // Adicionar evento para o botão "Esqueci minha senha"
    const forgotPasswordLink = document.querySelector('.forgot-password');
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', handleForgotPassword);
    }
}

function showSignUpModal() {
    closeAllModals();
    const modal = document.getElementById('signUpModal');
    modal?.classList.add('show');
    document.body.style.overflow = 'hidden';

    setTimeout(() => {
        document.getElementById('signupName')?.focus();
    }, 100);
}

function showSignInModal() {
    closeAllModals();
    const modal = document.getElementById('signInModal');
    modal?.classList.add('show');
    document.body.style.overflow = 'hidden';

    setTimeout(() => {
        document.getElementById('signinEmail')?.focus();
    }, 100);
}

function closeSignUpModal() {
    const modal = document.getElementById('signUpModal');
    modal?.classList.remove('show');
    document.body.style.overflow = '';
    clearForm('signUpForm');
}

function closeSignInModal() {
    const modal = document.getElementById('signInModal');
    modal?.classList.remove('show');
    document.body.style.overflow = '';
    clearForm('signInForm');
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('show');
    });
    document.body.style.overflow = '';
}

async function handleSignUp(e) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);

    if (!validateSignUpForm()) {
        return;
    }

    form.classList.add('loading');
    try {
        const response = await fetch('/api/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: formData.get('nomeCompleto'),
                email: formData.get('email'),
                password: formData.get('password')
            })
        });

        const result = await response.json();

        if (response.ok) {
            showMessage('signUpForm', 'Conta criada com sucesso! Verifique seu email para ativar a conta.', 'success');
            setTimeout(() => {
                if (window.location.pathname.includes('/signup')) {
                    window.location.href = '/login';
                } else {
                    closeSignUpModal();
                    showSignInModal();
                }
            }, 3000);
        } else {
            showMessage('signUpForm', result.error || 'Erro ao criar conta.', 'error');
        }
    } catch (error) {
        showMessage('signUpForm', 'Erro de conexão. Tente novamente.', 'error');
    } finally {
        form.classList.remove('loading');
    }
}

async function handleSignIn(e) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);
    form.classList.add('loading');
    try {
        const response = await fetch('/api/signin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: formData.get('email'),
                password: formData.get('password')
            })
        });

        if (response.ok) {
            showMessage('signInForm', 'Login realizado com sucesso! Redirecionando...', 'success');
            setTimeout(() => {
                window.location.href = '/movies';
            }, 1500);
        } else {
            const result = await response.json();
            if (result.error && result.error.includes('verificado')) {
                showMessage('signInForm', 'Sua conta não foi verificada. Verifique seu email antes de fazer login.', 'error');
            } else {
                showMessage('signInForm', 'Email ou senha incorretos.', 'error');
            }
        }
    } catch (error) {
        showMessage('signInForm', 'Erro de conexão. Tente novamente.', 'error');
    } finally {
        form.classList.remove('loading');
    }
}

function validateSignUpForm() {
    const name = document.getElementById('signupName');
    const email = document.getElementById('signupEmail');
    const password = document.getElementById('signupPassword');
    const confirmPassword = document.getElementById('signupConfirmPassword');
    const terms = document.getElementById('terms');

    let isValid = true;

    if (name.value.trim().length < 3) {
        showFieldError(name, 'Nome deve ter pelo menos 3 caracteres');
        isValid = false;
    } else {
        clearFieldError(name);
    }

    if (!isValidEmail(email.value)) {
        showFieldError(email, 'Email inválido');
        isValid = false;
    } else {
        clearFieldError(email);
    }

    if (password.value.length < 6) {
        showFieldError(password, 'Senha deve ter pelo menos 6 caracteres');
        isValid = false;
    } else {
        clearFieldError(password);
    }

    if (password.value !== confirmPassword.value) {
        showFieldError(confirmPassword, 'As senhas não coincidem');
        isValid = false;
    } else {
        clearFieldError(confirmPassword);
    }

    if (!terms.checked) {
        showMessage('signUpForm', 'Você deve aceitar os termos de serviço', 'error');
        isValid = false;
    }

    return isValid;
}

function validateEmail(e) {
    const input = e.target;
    if (!isValidEmail(input.value) && input.value.length > 0) {
        showFieldError(input, 'Email inválido');
    } else {
        clearFieldError(input);
    }
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validatePasswordMatch() {
    const password = document.getElementById('signupPassword');
    const confirmPassword = document.getElementById('signupConfirmPassword');

    if (confirmPassword.value.length > 0 && password.value !== confirmPassword.value) {
        showFieldError(confirmPassword, 'As senhas não coincidem');
    } else {
        clearFieldError(confirmPassword);
    }
}

function checkPasswordStrength(e) {
    const password = e.target.value;
    const strengthBar = document.querySelector('.strength-bar');

    if (!strengthBar) return;

    let strength = 0;

    if (password.length >= 8) strength++;

    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;

    if (/\d/.test(password)) strength++;

    if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength++;

    strengthBar.className = 'strength-bar';
    if (password.length > 0) {
        if (strength <= 1) {
            strengthBar.classList.add('strength-weak');
        } else if (strength <= 2) {
            strengthBar.classList.add('strength-medium');
        } else {
            strengthBar.classList.add('strength-strong');
        }
    }
}

function showFieldError(input, message) {
    const error = input.nextElementSibling;
    if (error && error.classList.contains('form-error')) {
        error.textContent = message;
        input.classList.add('error');
    }
}

function clearFieldError(input) {
    const error = input.nextElementSibling;
    if (error && error.classList.contains('form-error')) {
        error.textContent = '';
        input.classList.remove('error');
    }
}

function showMessage(formId, message, type) {
    const form = document.getElementById(formId);
    const existingMessage = form.querySelector('.auth-message');

    if (existingMessage) {
        existingMessage.remove();
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = `auth-message ${type}`;
    messageDiv.textContent = message;

    form.insertBefore(messageDiv, form.firstChild);

    setTimeout(() => {
        messageDiv.remove();
    }, 5000);
}

function clearForm(formId) {
    const form = document.getElementById(formId);
    if (form) {
        form.reset();
        form.querySelectorAll('.form-error').forEach(error => {
            error.textContent = '';
        });
        form.querySelectorAll('.error').forEach(input => {
            input.classList.remove('error');
        });
        const message = form.querySelector('.auth-message');
        if (message) message.remove();
    }
}

async function handleSocialAuth(e) {
    const button = e.currentTarget;
    const provider = button.classList.contains('google') ? 'google' : 'facebook';

    console.log(`Initiating ${provider} authentication...`);

    button.disabled = true;
    button.style.opacity = '0.6';

    try {
        window.location.href = `/auth/${provider}`;
    } catch (error) {
        console.error('Social authentication error:', error);
        showNotification('Erro ao conectar. Tente novamente.', 'error');
    } finally {
        button.disabled = false;
        button.style.opacity = '1';
    }
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        const activeModal = document.querySelector('.modal.show');
        if (activeModal) {
            if (activeModal.id === 'signUpModal') {
                closeSignUpModal();
            } else if (activeModal.id === 'signInModal') {
                closeSignInModal();
            }
        }
    }
});

document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal')) {
        const modalId = e.target.id;
        if (modalId === 'signUpModal') {
            closeSignUpModal();
        } else if (modalId === 'signInModal') {
            closeSignInModal();
        }
    }
});

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
    `;

    if (type === 'success') {
        notification.style.backgroundColor = '#4caf50';
    } else if (type === 'error') {
        notification.style.backgroundColor = '#f44336';
    }

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

function updateHeaderForLoggedUser(user) {
    const signInBtn = document.querySelector('.sign-in-btn');

    if (signInBtn) {
        const userMenu = document.createElement('div');
        userMenu.className = 'user-menu';
        userMenu.innerHTML = `
            <button class="user-menu-toggle">
                <img src="${user.avatar || '/images/default-avatar.png'}" alt="${user.name}" class="user-avatar">
                <span class="user-name">${user.name}</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
            </button>
            <div class="user-dropdown">
                <a href="/profile" class="dropdown-item">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    Meu Perfil
                </a>
                <a href="/watchlist" class="dropdown-item">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
                    </svg>
                    Minha Lista
                </a>
                <a href="/settings" class="dropdown-item">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <circle cx="12" cy="12" r="3"></circle>
                        <path d="M12 1v6m0 6v6m9-9h-6m-6 0H3m20.1 6.4l-4.26-4.26M7.86 7.86L3.6 3.6m16.8 0l-4.26 4.26M7.86 16.14L3.6 20.4"></path>
                    </svg>
                    Configurações
                </a>
                <div class="dropdown-divider"></div>
                <a href="/logout" class="dropdown-item logout">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                        <polyline points="16 17 21 12 16 7"></polyline>
                        <line x1="21" y1="12" x2="9" y2="12"></line>
                    </svg>
                    Sair
                </a>
            </div>
        `;

        signInBtn.parentNode.replaceChild(userMenu, signInBtn);

        const toggle = userMenu.querySelector('.user-menu-toggle');
        const dropdown = userMenu.querySelector('.user-dropdown');

        toggle.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.classList.toggle('show');
        });

        document.addEventListener('click', () => {
            dropdown.classList.remove('show');
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    checkAuthStatus();
});

async function checkAuthStatus() {
    try {
        const response = await fetch('/api/auth/status');
        if (response.ok) {
            const data = await response.json();
            if (data.isAuthenticated) {
                updateHeaderForLoggedUser(data.user);
            }
        }
    } catch (error) {
        console.error('Error checking auth status:', error);
    }
}

function handleForgotPassword(e) {
    e.preventDefault();
    
    // Fechar modal de login se estiver aberto
    closeSignInModal();
    
    // Redirecionar para a página de esqueci senha
    window.location.href = '/forgot-password';
}

window.authFunctions = {
    showSignUpModal,
    showSignInModal,
    closeSignUpModal,
    closeSignInModal,
    updateHeaderForLoggedUser,
    handleForgotPassword
};