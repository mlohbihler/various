const getRequiredElementById = (id: string) => {
  const ele = document.getElementById(id)
  if (!ele) {
    throw Error(`Required element with id '${id}' not found`)
  }
  return ele
}

const createElement = (parent: HTMLElement, html: string, attributes: { [key: string]: string } = {}): HTMLElement => {
  parent.insertAdjacentHTML('beforeend', html)
  const ele = parent.lastChild as HTMLElement
  Object.keys(attributes).forEach(k => ele.setAttribute(k, attributes[k]))
  return ele
}

window.onload = () => {
  createSudoku()
}

const BOX_SIZE = 3
const SIZE = 9

let state = [
  // Diabolical
  [' ', '3', ' ', '2', '6', ' ', '1', ' ', ' '],
  [' ', '6', ' ', '8', ' ', ' ', '3', '2', '4'],
  [' ', ' ', ' ', ' ', ' ', '1', ' ', ' ', ' '],
  [' ', ' ', '1', ' ', '8', ' ', ' ', '9', '2'],
  [' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '],
  ['4', '9', ' ', ' ', '2', ' ', '5', ' ', ' '],
  [' ', ' ', ' ', '6', ' ', ' ', ' ', ' ', ' '],
  ['8', '5', '9', ' ', ' ', '2', ' ', '6', ' '],
  [' ', ' ', '7', ' ', '5', '3', ' ', '8', ' '],
]

const updateState = (newState: string[][]) => {
  state = newState
  _0to8Sq((row, col) => {
    getRequiredElementById(cellName(row, col)).setAttribute('value', state[row][col])
  })
}

const cellName = (row: number, col: number) => `cell-${row}-${col}`

const createCell = (row: number, col: number) => {
  return `<input id="${cellName(row, col)}" type="text" />`
}

const createSection = (row: number, col: number) => {
  return `
    <td>
      <table>
        <tbody>
          <tr>
            <td>${createCell(row * 3 + 0, col * 3 + 0)}</td>
            <td>${createCell(row * 3 + 0, col * 3 + 1)}</td>
            <td>${createCell(row * 3 + 0, col * 3 + 2)}</td>
          </tr>
          <tr>
            <td>${createCell(row * 3 + 1, col * 3 + 0)}</td>
            <td>${createCell(row * 3 + 1, col * 3 + 1)}</td>
            <td>${createCell(row * 3 + 1, col * 3 + 2)}</td>
          </tr>
          <tr>
            <td>${createCell(row * 3 + 2, col * 3 + 0)}</td>
            <td>${createCell(row * 3 + 2, col * 3 + 1)}</td>
            <td>${createCell(row * 3 + 2, col * 3 + 2)}</td>
          </tr>
        </tbody>
      </table>
    </td>
  `
}

const createSudoku = () => {
  createElement(
    getRequiredElementById('sudoku'),
    `
      <table>
        <tbody>
          <tr>
            ${createSection(0, 0)}
            ${createSection(0, 1)}
            ${createSection(0, 2)}
          </tr>
          <tr>
            ${createSection(1, 0)}
            ${createSection(1, 1)}
            ${createSection(1, 2)}
          </tr>
          <tr>
            ${createSection(2, 0)}
            ${createSection(2, 1)}
            ${createSection(2, 2)}
          </tr>
        </tbody>
      </table>
    `,
  )

  const buttons = getRequiredElementById('sudoku-buttons')
  createElement(buttons, '<button type="button">Solve!</button>').addEventListener('click', solve)
  createElement(buttons, '<button type="button">Clear</button>').addEventListener('click', clear)
  createElement(buttons, '<button type="button">World&apos;s Hardest</button>').addEventListener('click', hardest)

  _0to8Sq((row, col) => {
    const cell = getRequiredElementById(cellName(row, col))
    cell.setAttribute('value', state[row][col])
    cell.addEventListener('change', e => cellChanged(<HTMLInputElement>e.target, row, col))
    cell.addEventListener('focus', e => (<HTMLInputElement>e.target).select())
  })
}

const cellChanged = (cell: HTMLInputElement, row: number, col: number) => {
  let newValue = cell.value
  if (newValue.length > 1) {
    newValue = newValue.substring(newValue.length - 1)
  }
  if (/^[ 1-9]$/.test(newValue)) {
    const copy: string[][] = []
    _1to9(i => copy.push(state[i - 1].slice()))
    copy[row][col] = newValue
    state = copy
  }
  cell.value = state[row][col]
  clearMessage()
}

const solve = () => {
  const data: number[][][] = []
  _0to8(i => {
    const row: number[][] = []
    data.push(row)
    _0to8(j => {
      const values: number[] = []
      row.push(values)
      const given = parseInt(state[i][j], 10)
      if (given >= 1 && given <= 9) {
        values.push(given)
      } else {
        _1to9(k => values.push(k))
      }
    })
  })
  const model = { data, changed: false }
  try {
    // First, validate the model
    _boxValidator(model)
    _horizontalValidator(model)
    _verticalValidator(model)

    // Solve
    _solve(model)

    // Validate again
    _boxValidator(model)
    _horizontalValidator(model)
    _verticalValidator(model)

    // Successfully validated. Put the results back into the puzzle.
    const solvedData: string[][] = []
    _0to8(r => {
      const row: string[] = []
      _0to8(c => row.push(model.data[r][c][0].toString()))
      solvedData.push(row)
    })

    updateState(solvedData)
    // Display results depending on whether the puzzle was actually solved or not.
    if (_isSolved(model)) {
      setMessage('Puzzle successfully solved')
    } else {
      setMessage('Puzzle not solved')
    }
  } catch (e) {
    // Uh oh, there was an error while solving. This could be due to an entry error by the user, or, face it, a bug.
    // Let's just display the error message for now.
    setMessage(
      'There was an error during solving. This could be due to an entry error, or, well, a bug. But be a sport and check what you entered.',
    )
    console.log(e)
  }
}

const clear = () => {
  const data: string[][] = []
  _1to9(() => {
    const row: string[] = []
    data.push(row)
    _1to9(() => row.push(''))
  })
  updateState(data)
  clearMessage()
}

const hardest = () => {
  // World's hardest
  updateState([
    ['8', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '], //
    [' ', ' ', '3', '6', ' ', ' ', ' ', ' ', ' '], //
    [' ', '7', ' ', ' ', '9', ' ', '2', ' ', ' '], //
    [' ', '5', ' ', ' ', ' ', '7', ' ', ' ', ' '], //
    [' ', ' ', ' ', ' ', '4', '5', '7', ' ', ' '], //
    [' ', ' ', ' ', '1', ' ', ' ', ' ', '3', ' '], //
    [' ', ' ', '8', '5', ' ', ' ', ' ', '1', ' '], //
    [' ', ' ', '1', ' ', ' ', ' ', ' ', '6', '8'], //
    [' ', '9', ' ', ' ', ' ', ' ', '4', ' ', ' '], //
  ])
  clearMessage()
}

const clearMessage = () => {
  getRequiredElementById('sudoku-message-container').replaceChildren()
}

const setMessage = (message: string) => {
  createElement(getRequiredElementById('sudoku-message-container'), `<span id="sudoku-message">${message}</span>`)
}

const _1to9 = (fn: (i: number) => void) => {
  _xtoy(1, 10, fn)
}
const _0to8 = (fn: (i: number) => void) => {
  _xtoy(0, 9, fn)
}
const _xtoy = (start: number, end: number, fn: (i: number) => void) => {
  for (let i = start; i < end; i++) {
    fn(i)
  }
}

const _1to9Sq = (fn: (i: number, j: number) => void) => {
  _1to9(i => _1to9(j => fn(i, j)))
}
const _0to8Sq = (fn: (i: number, j: number) => void) => {
  _0to8(i => _0to8(j => fn(i, j)))
}
const _xtoySq = (start: number, end: number, fn: (i: number, j: number) => void) => {
  _xtoy(start, end, i => _xtoy(start, end, j => fn(i, j)))
}

const _solve = (model: Model) => {
  let changed = false
  while (true) {
    changed = _solveWithSolvers(model, _firstSolvers)

    if (!_isSolved(model)) {
      if (_solveWithSolvers(model, _secondSolvers)) {
        changed = true
      }
    }

    if (_isSolved(model) || !changed) {
      break
    }

    changed = false
  }
}

const _solveWithSolvers = (model: Model, solvers: Solvers) => {
  let changed = false
  model.changed = false
  while (true) {
    solvers.forEach(s => s(model))
    if (!model.changed) {
      break
    }
    changed = true
    model.changed = false
  }
  return changed
}

const _isCellSolved = (model: Model, x: number, y: number) => {
  return model.data[x][y].length === 1
}

const _getSolvedValue = (model: Model, x: number, y: number) => {
  return model.data[x][y][0]
}

const _isSolved = (model: Model) => {
  for (let y = 0; y < SIZE; y++) {
    for (let x = 0; x < SIZE; x++) {
      if (!_isCellSolved(model, x, y)) {
        return false
      }
    }
  }
  return true
}

const _removeValues = (model: Model, x: number, y: number, remove: number[]) => {
  const values = model.data[x][y]
  remove.forEach(e => {
    let pos = values.indexOf(e)
    if (pos >= 0) {
      values.splice(pos, 1)
      model.changed = true
    }
  })
}

const _setValue = (model: Model, x: number, y: number, value: number) => {
  if (model.data[x][y].indexOf(value) === -1) {
    throw Error(
      'Attempt to set cell to a value that has already been eliminated: cell:' + model.data[x][y] + ', value=' + value,
    )
  }

  // Check if the cell will actually change.
  if (model.data[x][y].length === 1 && model.data[x][y][0] === value) {
    return
  }

  model.data[x][y] = [value]
  model.changed = true
}

//
// Solvers
//
const _boxEliminator = (model: Model) => {
  // For each section
  _xtoySq(0, BOX_SIZE, (i, j) => {
    const xStart = i * BOX_SIZE
    const yStart = j * BOX_SIZE

    // Collect a list of all solved cells.
    const solved: number[] = []
    _xtoy(xStart, BOX_SIZE + xStart, x => {
      _xtoy(yStart, BOX_SIZE + yStart, y => {
        if (_isCellSolved(model, x, y)) {
          solved.push(_getSolvedValue(model, x, y))
        }
      })
    })

    // Remove these solved values from unsolved cells.
    _xtoy(xStart, BOX_SIZE + xStart, x => {
      _xtoy(yStart, BOX_SIZE + yStart, y => {
        if (!_isCellSolved(model, x, y)) {
          _removeValues(model, x, y, solved)
        }
      })
    })
  })
}

const _verticalEliminator = (model: Model) => {
  _0to8(col => {
    // Collect a list of all solved cells.
    const solved: number[] = []
    _0to8(i => {
      if (_isCellSolved(model, i, col)) {
        solved.push(_getSolvedValue(model, i, col))
      }
    })

    // Remove these solved values from unsolved cells.
    _0to8(i => {
      if (!_isCellSolved(model, i, col)) {
        _removeValues(model, i, col, solved)
      }
    })
  })
}

const _horizontalEliminator = (model: Model) => {
  _0to8(row => {
    // Collect a list of all solved cells.
    const solved: number[] = []
    _0to8(i => {
      if (_isCellSolved(model, row, i)) {
        solved.push(_getSolvedValue(model, row, i))
      }
    })

    // Remove these solved values from unsolved cells.
    _0to8(i => {
      if (!_isCellSolved(model, row, i)) {
        _removeValues(model, row, i, solved)
      }
    })
  })
}

const _boxSingleValueSeeker = (model: Model) => {
  const FOUND_POINT = -1

  _xtoySq(0, BOX_SIZE, (i, j) => {
    const xStart = i * BOX_SIZE
    const yStart = j * BOX_SIZE

    _1to9(value => {
      let point: [number, number] | number | null = null
      _xtoy(xStart, BOX_SIZE + xStart, x => {
        _xtoy(yStart, BOX_SIZE + yStart, y => {
          if (model.data[x][y].indexOf(value) !== -1) {
            if (point == null) {
              point = [x, y]
            } else {
              point = FOUND_POINT
            }
          }
        })
      })

      if (point === null) {
        throw Error('No cell can take the value ' + value + ', box at ' + xStart + ',' + yStart)
      }

      if (point !== FOUND_POINT && !_isCellSolved(model, point[0], point[1])) {
        // Only one cell can take the value, so set the cell to that value.
        _setValue(model, point[0], point[1], value)
      }
    })
  })
}

const _horizontalSingleValueSeeker = (model: Model) => {
  _0to8Sq((row, v) => {
    const value = v + 1
    let column = -1
    _0to8(i => {
      if (model.data[i][row].indexOf(value) !== -1) {
        if (column === -1) {
          column = i
        } else {
          column = -2
        }
      }
    })

    if (column === -1) {
      throw Error('No cell can take the value ' + value + ', row ' + row)
    }

    if (column > -1 && !_isCellSolved(model, column, row)) {
      // Only one cell can take the value, so set the cell to that value.
      _setValue(model, column, row, value)
    }
  })
}

const _verticalSingleValueSeeker = (model: Model) => {
  _0to8Sq((col, v) => {
    const value = v + 1
    let row = -1
    _0to8(i => {
      if (model.data[col][i].indexOf(value) !== -1) {
        if (row === -1) {
          row = i
        } else {
          row = -2
        }
      }
    })

    if (row === -1) {
      throw Error('No cell can take the value ' + value + ', column ' + col)
    }

    if (row > -1 && !_isCellSolved(model, col, row)) {
      // Only one cell can take the value, so set the cell to that value.
      _setValue(model, col, row, value)
    }
  })
}

const _potentialValueElimination = (model: Model) => {
  try {
    // Iterate through the cells looking for those with multiple potential values.
    _0to8Sq((x, y) => {
      if (!_isCellSolved(model, x, y)) {
        const values = model.data[x][y].slice()

        // Test the values in a clone.
        values.forEach(value => {
          const dataClone: number[][][] = []
          _0to8(i => {
            const row: number[][] = []
            dataClone.push(row)
            _0to8(j => row.push(model.data[i][j].slice()))
          })

          const modelClone = { data: dataClone, changed: false }
          _setValue(modelClone, x, y, value)

          let err = false
          try {
            _solve(modelClone)
          } catch (e) {
            // The value didn't work, so remove it as a potential value.
            _removeValues(model, x, y, [value])
            err = true
            if (model.data[x][y].length === 0) {
              throw Error('No values remain at ' + x + ', ' + y)
            }
          }

          if (!err) {
            if (_isSolved(modelClone)) {
              model.data = modelClone.data
              model.changed = modelClone.changed
              // Throw an error to escape from the loops. This is caught below.
              throw Error('solved')
            }
          }
        })
      }
    })
  } catch (e) {
    // Catches the solved exception. Rethrows all others.
    if ((<Error>e).message !== 'solved') {
      throw e
    }
  }
}

type Solvers = ((model: Model) => void)[]

const _firstSolvers = [
  _boxEliminator,
  _horizontalEliminator,
  _verticalEliminator,
  _boxSingleValueSeeker,
  _horizontalSingleValueSeeker,
  _verticalSingleValueSeeker,
]

const _secondSolvers = [_potentialValueElimination]

//
// Validators
//
type Model = {
  data: number[][][]
  changed: boolean
}

const _boxValidator = (model: Model) => {
  _xtoySq(0, BOX_SIZE, (i, j) => {
    const xStart = i * BOX_SIZE
    const yStart = j * BOX_SIZE
    const values: number[] = []

    _xtoy(xStart, BOX_SIZE + xStart, x => {
      _xtoy(yStart, BOX_SIZE + yStart, y => {
        if (_isCellSolved(model, x, y)) {
          const value = model.data[x][y][0]
          // Check that the value doesn't already exist.
          if (values.indexOf(value) !== -1) {
            throw Error('Duplicate box value at x=' + x + ', y=' + y)
          }
          values.push(value)
        }
      })
    })
  })
}

const _horizontalValidator = (model: Model) => {
  _0to8(row => {
    const values: number[] = []

    // Ensure that there are no duplicate solved cells.
    _0to8(x => {
      if (_isCellSolved(model, x, row)) {
        const value = model.data[x][row][0]
        // Check that the value doesn't already exist.
        if (values.indexOf(value) !== -1) {
          throw Error('Duplicate row value at x=' + x + ', y=' + row)
        }
        values.push(value)
      }
    })
  })
}

const _verticalValidator = (model: Model) => {
  _0to8(col => {
    const values: number[] = []

    // Ensure that there are no duplicate solved cells.
    _0to8(y => {
      if (_isCellSolved(model, col, y)) {
        const value = model.data[col][y][0]
        // Check that the value doesn't already exist.
        if (values.indexOf(value) !== -1) {
          throw Error('Duplicate column value at x=' + col + ', y=' + y)
        }
        values.push(value)
      }
    })
  })
}
