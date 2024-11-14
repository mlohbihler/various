import React from 'react';
import ReactDOM from 'react-dom';
import Holder from 'holderjs/holder.js'
import App from './App';
import registerServiceWorker from './registerServiceWorker';

import './index.css';
import 'bootstrap/dist/css/bootstrap.min.css'

Holder.addTheme('thumb', {
  bg: '#55595c',
  fg: '#eceeef',
  text: 'Thumbnail'
});

ReactDOM.render(<App />, document.getElementById('root'));
registerServiceWorker();
