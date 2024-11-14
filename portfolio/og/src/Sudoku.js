import React, { Component } from 'react'
import { Alert, Button } from 'reactstrap'

import './Sudoku.css'

const BOX_SIZE = 3
const SIZE = 9

const Cell = (props) => <input type="text" value={props.d[props.r][props.c]}
                            onChange={e => props.f(e.target.value, props.r, props.c)}
                            onFocus={e => e.target.select()}/>

const Section = (props) => (
  <td>
    <table>
      <tbody>
        <tr>
          <td><Cell d={props.d} r={props.r * 3 + 0} c={props.c * 3 + 0} f={props.f}/></td>
          <td><Cell d={props.d} r={props.r * 3 + 0} c={props.c * 3 + 1} f={props.f}/></td>
          <td><Cell d={props.d} r={props.r * 3 + 0} c={props.c * 3 + 2} f={props.f}/></td>
        </tr>
        <tr>
          <td><Cell d={props.d} r={props.r * 3 + 1} c={props.c * 3 + 0} f={props.f}/></td>
          <td><Cell d={props.d} r={props.r * 3 + 1} c={props.c * 3 + 1} f={props.f}/></td>
          <td><Cell d={props.d} r={props.r * 3 + 1} c={props.c * 3 + 2} f={props.f}/></td>
        </tr>
        <tr>
          <td><Cell d={props.d} r={props.r * 3 + 2} c={props.c * 3 + 0} f={props.f}/></td>
          <td><Cell d={props.d} r={props.r * 3 + 2} c={props.c * 3 + 1} f={props.f}/></td>
          <td><Cell d={props.d} r={props.r * 3 + 2} c={props.c * 3 + 2} f={props.f}/></td>
        </tr>
      </tbody>
    </table>
  </td>
)

export default class Sudoku extends Component {
  constructor(props) {
    super(props)

    this.state = {
      data: [
        // Diabolical
        [ ' ', '3', ' ', '2', '6', ' ', '1', ' ', ' ' ],
        [ ' ', '6', ' ', '8', ' ', ' ', '3', '2', '4' ],
        [ ' ', ' ', ' ', ' ', ' ', '1', ' ', ' ', ' ' ],
        [ ' ', ' ', '1', ' ', '8', ' ', ' ', '9', '2' ],
        [ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' ],
        [ '4', '9', ' ', ' ', '2', ' ', '5', ' ', ' ' ],
        [ ' ', ' ', ' ', '6', ' ', ' ', ' ', ' ', ' ' ],
        [ '8', '5', '9', ' ', ' ', '2', ' ', '6', ' ' ],
        [ ' ', ' ', '7', ' ', '5', '3', ' ', '8', ' ' ],
      ]
    }

    this.updateCell = this.updateCell.bind(this)
    this.solve = this.solve.bind(this)
    this.clear = this.clear.bind(this)
    this.hardest = this.hardest.bind(this)
  }

  updateCell(value, x, y) {
    if (value.length > 1) {
      value = value.substring(value.length - 1)
    }
    if (/^[ 1-9]$/.test(value)) {
      const copy = []
      this._1to9(i => copy.push(this.state.data[i-1].slice()))
      copy[x][y] = value
      this.setState({data: copy})
    }
    this.clearMessages()
  }

  solve() {
    const data = []
    this._0to8(i => {
      const row = []
      data.push(row)
      this._0to8(j => {
        const values = []
        row.push(values)
        const given = parseInt(this.state.data[i][j], 10)
        if (given >= 1 && given <= 9) {
          values.push(given)
        } else {
          this._1to9(k => values.push(k))
        }
      })
    })

    const model = { data, changed: false }

    try {
      // First, validate the model
      this._boxValidator(model)
      this._horizontalValidator(model)
      this._verticalValidator(model)

      // Solve
      this._solve(model)

      // Validate again
      this._boxValidator(model)
      this._horizontalValidator(model)
      this._verticalValidator(model)

      // Successfully validated. Put the results back into the puzzle.
      const solvedData = []
      this._0to8(i => solvedData.push(model.data[i].slice()))
      const state = {data: solvedData}

      // Display results depending on whether the puzzle was actually solved or not.
      if (this._isSolved(model)) {
        state['solvedMessage'] = 'Puzzle successfully solved'
      } else {
        state['notSolvedMessage'] = 'Puzzle not solved'
      }
      this.setState(state)
    } catch (e) {
      // Uh oh, there was an error while solving. This could be due to an entry error by the user, or, face it, a bug.
      // Let's just display the error message for now.
      this.setState({solveError: "There was an error during solving. This could be due to an entry error, or, well, a bug. But be a sport and check what you entered."})
      console.log(e)
    }
  }

  clear() {
    const data = []
    this._1to9(i => {
      const row = []
      data.push(row)
      this._1to9(j => row.push(''))
    })
    this.setState({data})
    this.clearMessages()
  }

  hardest() {
    // World's hardest
    this.setState({data: [
        [ '8', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' ], //
        [ ' ', ' ', '3', '6', ' ', ' ', ' ', ' ', ' ' ], //
        [ ' ', '7', ' ', ' ', '9', ' ', '2', ' ', ' ' ], //
        [ ' ', '5', ' ', ' ', ' ', '7', ' ', ' ', ' ' ], //
        [ ' ', ' ', ' ', ' ', '4', '5', '7', ' ', ' ' ], //
        [ ' ', ' ', ' ', '1', ' ', ' ', ' ', '3', ' ' ], //
        [ ' ', ' ', '8', '5', ' ', ' ', ' ', '1', ' ' ], //
        [ ' ', ' ', '1', ' ', ' ', ' ', ' ', '6', '8' ], //
        [ ' ', '9', ' ', ' ', ' ', ' ', '4', ' ', ' ' ], //
      ]
    })
    this.clearMessages()
  }

  clearMessages() {
    this.setState({solvedMessage: null, notSolvedMessage: null, solveError: null})
  }

  render() {
    let solvedMessage, notSolvedMessage, solveError
    if (this.state.solvedMessage) {
      solvedMessage = <Alert color="success">{this.state.solvedMessage}</Alert>
    }
    if (this.state.notSolvedMessage) {
      solvedMessage = <Alert color="warning">{this.state.notSolvedMessage}</Alert>
    }
    if (this.state.solveError) {
      solvedMessage = <Alert color="danger">{this.state.solveError}</Alert>
    }

    return (
      <div className="text-center">
        <div id="sudoku">
          <table>
            <tbody>
              <tr>
                <Section d={this.state.data} r={0} c={0} f={this.updateCell}/>
                <Section d={this.state.data} r={0} c={1} f={this.updateCell}/>
                <Section d={this.state.data} r={0} c={2} f={this.updateCell}/>
              </tr>
              <tr>
                <Section d={this.state.data} r={1} c={0} f={this.updateCell}/>
                <Section d={this.state.data} r={1} c={1} f={this.updateCell}/>
                <Section d={this.state.data} r={1} c={2} f={this.updateCell}/>
              </tr>
              <tr>
                <Section d={this.state.data} r={2} c={0} f={this.updateCell}/>
                <Section d={this.state.data} r={2} c={1} f={this.updateCell}/>
                <Section d={this.state.data} r={2} c={2} f={this.updateCell}/>
              </tr>
            </tbody>
          </table>

          <div className='btns'>
            <Button color="primary" onClick={this.solve}>Solve!</Button>{' '}
            <Button color="secondary" onClick={this.clear}>Clear</Button>{' '}
            <Button color="secondary" onClick={this.hardest}>World&apos;s Hardest</Button>
          </div>

          {solvedMessage}
          {notSolvedMessage}
          {solveError}
        </div>
      </div>
    )
  }

  _1to9(fn) { this._xtoy(1, 10, fn) }
  _0to8(fn) { this._xtoy(0, 9, fn) }
  _xtoy(start, end, fn) {
    for (let i=start; i<end; i++) {
      fn(i)
    }
  }

  _1to9Sq(fn) { this._1to9(i => this._1to9(j => fn(i, j))) }
  _0to8Sq(fn) { this._0to8(i => this._0to8(j => fn(i, j))) }
  _xtoySq(start, end, fn) { this._xtoy(start, end, i => this._xtoy(start, end, j => fn(i, j))) }

  _solve(model) {
      let changed = false
      while (true) {
        changed = this._solveWithSolvers(model, this._firstSolvers)

        if (!this._isSolved(model)) {
          if (this._solveWithSolvers(model, this._secondSolvers)) {
            changed = true
          }
        }

        if (this._isSolved(model) || !changed) {
          break
        }

        changed = false
      }
  }

  _solveWithSolvers(model, solvers) {
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

  _isCellSolved(model, x, y) {
    return model.data[x][y].length === 1
  }

  _getSolvedValue(model, x, y) {
    return model.data[x][y][0]
  }

  _isSolved(model) {
    for (let y = 0; y < SIZE; y++) {
      for (let x = 0; x < SIZE; x++) {
        if (!this._isCellSolved(model, x, y)) {
          return false
        }
      }
    }
    return true
  }

  _removeValues(model, x, y, remove) {
    const values = model.data[x][y]
    remove.forEach(e => {
      let pos = values.indexOf(e)
      if (pos >= 0) {
        values.splice(pos, 1)
        model.changed = true
      }
    })
  }

  _setValue(model, x, y, value) {
    if (model.data[x][y].indexOf(value) === -1) {
      throw Error("Attempt to set cell to a value that has already been eliminated: cell:" + model.data[x][y] + ", value=" + value)
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
  _boxEliminator(model) {
    // For each section
    this._xtoySq(0, BOX_SIZE, (i, j) => {
      const xStart = i * BOX_SIZE
      const yStart = j * BOX_SIZE

      // Collect a list of all solved cells.
      const solved = []
      this._xtoy(xStart, BOX_SIZE + xStart, x => {
        this._xtoy(yStart, BOX_SIZE + yStart, y => {
          if (this._isCellSolved(model, x, y)) {
            solved.push(this._getSolvedValue(model, x, y))
          }
        })
      })

      // Remove these solved values from unsolved cells.
      this._xtoy(xStart, BOX_SIZE + xStart, x => {
        this._xtoy(yStart, BOX_SIZE + yStart, y => {
          if (!this._isCellSolved(model, x, y)) {
            this._removeValues(model, x, y, solved)
          }
        })
      })
    })
  }

  _verticalEliminator(model) {
    this._0to8(col => {
      // Collect a list of all solved cells.
      const solved = []
      this._0to8(i => {
        if (this._isCellSolved(model, i, col)) {
          solved.push(this._getSolvedValue(model, i, col))
        }
      })

      // Remove these solved values from unsolved cells.
      this._0to8(i => {
        if (!this._isCellSolved(model, i, col)) {
          this._removeValues(model, i, col, solved)
        }
      })
    })
  }

  _horizontalEliminator(model) {
    this._0to8(row => {
      // Collect a list of all solved cells.
      const solved = []
      this._0to8(i => {
        if (this._isCellSolved(model, row, i)) {
          solved.push(this._getSolvedValue(model, row, i))
        }
      })

      // Remove these solved values from unsolved cells.
      this._0to8(i => {
        if (!this._isCellSolved(model, row, i)) {
          this._removeValues(model, row, i, solved)
        }
      })
    })
  }

  _boxSingleValueSeeker(model) {
    const FOUND_POINT = -1

    this._xtoySq(0, BOX_SIZE, (i, j) => {
      const xStart = i * BOX_SIZE
      const yStart = j * BOX_SIZE

      this._1to9(value => {
        let point = null
        this._xtoy(xStart, BOX_SIZE + xStart, x => {
          this._xtoy(yStart, BOX_SIZE + yStart, y => {
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
          throw Error("No cell can take the value " + value + ", box at " + xStart + "," + yStart)
        }

        if (point !== FOUND_POINT && !this._isCellSolved(model, point[0], point[1])) {
          // Only one cell can take the value, so set the cell to that value.
          this._setValue(model, point[0], point[1], value)
        }
      })
    })
  }

  _horizontalSingleValueSeeker(model) {
    this._0to8Sq((row, v) => {
      const value = v + 1
      let column = -1
      this._0to8(i => {
        if (model.data[i][row].indexOf(value) !== -1) {
          if (column === -1) {
              column = i
          } else {
              column = -2
          }
        }
      })

      if (column === -1) {
        throw Error("No cell can take the value " + value +", row " + row)
      }

      if (column > -1 && !this._isCellSolved(model, column, row)) {
        // Only one cell can take the value, so set the cell to that value.
        this._setValue(model, column, row, value)
      }
    })
  }

  _verticalSingleValueSeeker(model) {
    this._0to8Sq((col, v) => {
      const value = v + 1
      let row = -1
      this._0to8(i => {
        if (model.data[col][i].indexOf(value) !== -1) {
          if (row === -1) {
              row = i
          } else {
              row = -2
          }
        }
      })

      if (row === -1) {
        throw Error("No cell can take the value " + value +", column " + col)
      }

      if (row > -1 && !this._isCellSolved(model, col, row)) {
        // Only one cell can take the value, so set the cell to that value.
        this._setValue(model, col, row, value)
      }
    })
  }

  _potentialValueElimination(model) {
    try {
      // Iterate through the cells looking for those with multiple potential values.
      this._0to8Sq((x, y) => {
        if (!this._isCellSolved(model, x, y)) {
          const values = model.data[x][y].slice()

          // Test the values in a clone.
          values.forEach(value => {
            const dataClone = []
            this._0to8(i => {
              const row = []
              dataClone.push(row)
              this._0to8(j => row.push(model.data[i][j].slice()))
            })

            const modelClone = {data: dataClone, changed: false}
            this._setValue(modelClone, x, y, value)

            let err = false
            try {
              this._solve(modelClone)
            } catch (e) {
              // The value didn't work, so remove it as a potential value.
              this._removeValues(model, x, y, [value])
              err = true
              if (model.data[x][y].length === 0) {
                throw Error("No values remain at " + x + ", " + y)
              }
            }

            if (!err) {
              if (this._isSolved(modelClone)) {
                model.data = modelClone.data
                model.changed = modelClone.changed
                // Throw an error to escape from the loops. This is caught below.
                throw Error("solved")
              }
            }
          })
        }
      })
    } catch (e) {
      // Catches the solved exception. Rethrows all others.
      if (e.message !== "solved") {
        throw e
      }
    }
  }

  _firstSolvers = [
    this._boxEliminator.bind(this),
    this._horizontalEliminator.bind(this),
    this._verticalEliminator.bind(this),
    this._boxSingleValueSeeker.bind(this),
    this._horizontalSingleValueSeeker.bind(this),
    this._verticalSingleValueSeeker.bind(this),
  ]

  _secondSolvers = [
    this._potentialValueElimination.bind(this),
  ]

  //
  // Validators
  //
  _boxValidator(model) {
    this._xtoySq(0, BOX_SIZE, (i, j) => {
      const xStart = i * BOX_SIZE
      const yStart = j * BOX_SIZE
      const values = []

      this._xtoy(xStart, BOX_SIZE + xStart, x => {
        this._xtoy(yStart, BOX_SIZE + yStart, y => {
          if (this._isCellSolved(model, x, y)) {
            const value = model.data[x][y][0]
            // Check that the value doesn't already exist.
            if (values.indexOf(value) !== -1) {
              throw Error("Duplicate box value at x=" + x + ", y=" + y)
            }
            values.push(value)
          }
        })
      })
    })
  }

  _horizontalValidator(model) {
    this._0to8(row => {
      const values = []

      // Ensure that there are no duplicate solved cells.
      this._0to8(x => {
        if (this._isCellSolved(model, x, row)) {
          const value = model.data[x][row][0]
          // Check that the value doesn't already exist.
          if (values.indexOf(value) !== -1) {
            throw Error("Duplicate row value at x=" + x + ", y=" + row)
          }
          values.push(value)
        }
      })
    })
  }

  _verticalValidator(model) {
    this._0to8(col => {
      const values = []

      // Ensure that there are no duplicate solved cells.
      this._0to8(y => {
        if (this._isCellSolved(model, col, y)) {
          const value = model.data[col][y][0]
          // Check that the value doesn't already exist.
          if (values.indexOf(value) !== -1) {
            throw Error("Duplicate column value at x=" + col + ", y=" + y)
          }
          values.push(value)
        }
      })
    })
  }
}
