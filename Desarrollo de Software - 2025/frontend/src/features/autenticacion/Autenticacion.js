import React, { useState } from 'react';
import { Modal } from 'react-bootstrap';
import './Autenticacion.css';
import { useAuth } from '../../context/Auth';
import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';
import { clickOnEnterKeyDown } from '../../utils/clickOnEnterKeyDown';

export function Autenticacion({ show, onHide }) {
  const [isLogin, setIsLogin] = useState(true);

  useAuth();

  const switchToRegister = () => {
    setIsLogin(false);
  };

  const switchToLogin = () => {
    setIsLogin(true);
  };

  return (
    <Modal show={show} onHide={onHide} centered>
      <Modal.Header closeButton className="auth-modal-header">
        <Modal.Title className="auth-modal-title">
          {isLogin ? 'Ingresa a tu usuario de Birbnb' : 'Registrate en Birbnb'}
        </Modal.Title>
      </Modal.Header>
      <Modal.Body className="auth-modal-body">
        {isLogin ? <LoginForm onHide={onHide} /> : <RegisterForm onHide={onHide} />}
        <div className="auth-switch-container">
          {isLogin ? (
            <p className="auth-switch-text">
              ¿Todavía no tienes cuenta?{' '}
              <span tabIndex={0} onKeyDown={clickOnEnterKeyDown} className="auth-switch-link" onClick={switchToRegister}>
                Regístrate
              </span>
            </p>
          ) : (
            <p className="auth-switch-text">
              ¿Ya tienes cuenta?{' '}
              <span tabIndex={0} onKeyDown={clickOnEnterKeyDown} className="auth-switch-link" onClick={switchToLogin}>
                Inicia sesión
              </span>
            </p>
          )}
        </div>
      </Modal.Body>
    </Modal >
  );
}