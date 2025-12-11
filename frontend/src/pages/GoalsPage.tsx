import { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Button,
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Chip,
  CircularProgress,
  Tabs,
  Tab,
  Select,
  FormControl,
  InputLabel,
} from '@mui/material';
import { Add, Edit, Delete, Assignment } from '@mui/icons-material';
import { goalService } from '@/services/goalService';
import { employeeService } from '@/services/employeeService';
import { Goal, GoalTemplate, CreateGoalRequest, AssignGoalRequest, UpdateGoalStatusRequest, Employee } from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { format } from 'date-fns';

const GoalsPage = () => {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [goalTemplates, setGoalTemplates] = useState<GoalTemplate[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [tabValue, setTabValue] = useState(0);
  const [openTemplateDialog, setOpenTemplateDialog] = useState(false);
  const [openAssignDialog, setOpenAssignDialog] = useState(false);
  const [openStatusDialog, setOpenStatusDialog] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<GoalTemplate | null>(null);
  const [selectedGoal, setSelectedGoal] = useState<Goal | null>(null);
  const { user } = useAuth();
  const isHR = user?.role?.toLowerCase() === 'hr';

  const [templateFormData, setTemplateFormData] = useState<CreateGoalRequest>({
    title: '',
    description: '',
    metric: '',
    reviewPeriod: 'Q4-2024',
  });

  const [assignFormData, setAssignFormData] = useState<AssignGoalRequest>({
    goalId: 0,
    employeeId: 0,
    dueDate: format(new Date(), 'yyyy-MM-dd'),
  });

  const [statusFormData, setStatusFormData] = useState<UpdateGoalStatusRequest>({
    status: '',
  });

  useEffect(() => {
    fetchData();
  }, [user]);

  useEffect(() => {
    // Обновляем данные при переключении вкладок для HR
    if (isHR && tabValue === 1) {
      fetchData();
    }
  }, [tabValue, isHR]);

  const fetchData = async () => {
    try {
      setLoading(true);
      if (isHR) {
        const [templates, employeesData, allAssignments] = await Promise.all([
          goalService.getAllGoalTemplates(),
          employeeService.getAllEmployees(),
          goalService.getAllGoalAssignments(),
        ]);
        setGoalTemplates(templates);
        setEmployees(employeesData);
        setGoals(allAssignments);
      } else {
        const myGoals = await goalService.getMyGoals();
        setGoals(myGoals);
      }
    } catch (error) {
      console.error('Failed to fetch data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenTemplateDialog = (template?: GoalTemplate) => {
    if (template) {
      setEditingTemplate(template);
      setTemplateFormData({
        title: template.title,
        description: template.description,
        metric: template.metric,
        reviewPeriod: template.reviewPeriod,
      });
    } else {
      setEditingTemplate(null);
      setTemplateFormData({
        title: '',
        description: '',
        metric: '',
        reviewPeriod: 'Q4-2024',
      });
    }
    setOpenTemplateDialog(true);
  };

  const handleCloseTemplateDialog = () => {
    setOpenTemplateDialog(false);
    setEditingTemplate(null);
  };

  const handleSubmitTemplate = async () => {
    if (!templateFormData.title.trim() || !templateFormData.description.trim() || !templateFormData.metric.trim()) {
      alert('Please fill all required fields');
      return;
    }

    try {
      if (editingTemplate) {
        await goalService.updateGoalTemplate(editingTemplate.id, templateFormData);
      } else {
        await goalService.createGoalTemplate(templateFormData);
      }
      fetchData();
      handleCloseTemplateDialog();
    } catch (error: any) {
      console.error('Failed to save goal template:', error);
      alert(error.response?.data?.message || 'Error saving goal template');
    }
  };

  const handleDeleteTemplate = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this goal template?')) {
      try {
        await goalService.deleteGoalTemplate(id);
        fetchData();
      } catch (error) {
        console.error('Failed to delete goal template:', error);
        alert('Error deleting goal template');
      }
    }
  };

  const handleOpenAssignDialog = (template: GoalTemplate) => {
    setAssignFormData({
      goalId: template.id,
      employeeId: 0,
      dueDate: format(new Date(), 'yyyy-MM-dd'),
    });
    setOpenAssignDialog(true);
  };

  const handleCloseAssignDialog = () => {
    setOpenAssignDialog(false);
  };

  const handleSubmitAssign = async () => {
    if (!assignFormData.employeeId || !assignFormData.dueDate) {
      alert('Please select an employee and due date');
      return;
    }

    try {
      await goalService.assignGoal(assignFormData);
      fetchData();
      handleCloseAssignDialog();
      alert('Goal successfully assigned to employee');
    } catch (error: any) {
      console.error('Failed to assign goal:', error);
      alert(error.response?.data?.message || 'Error assigning goal');
    }
  };

  const handleOpenStatusDialog = (goal: Goal) => {
    setSelectedGoal(goal);
    setStatusFormData({ status: goal.status });
    setOpenStatusDialog(true);
  };

  const handleCloseStatusDialog = () => {
    setOpenStatusDialog(false);
    setSelectedGoal(null);
  };

  const handleSubmitStatus = async () => {
    if (!selectedGoal) return;

    try {
      await goalService.updateGoalStatus(selectedGoal.id, statusFormData);
      fetchData();
      handleCloseStatusDialog();
    } catch (error: any) {
      console.error('Failed to update goal status:', error);
      alert(error.response?.data?.message || 'Error updating status');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'success';
      case 'in_progress':
        return 'primary';
      case 'not_started':
        return 'default';
      case 'blocked':
        return 'error';
      default:
        return 'default';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 3,
          gap: 2,
        }}
      >
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>
            Goals Management
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Create templates, assign tasks and track progress
          </Typography>
        </Box>
        {isHR && (
          <Button variant="contained" startIcon={<Add />} onClick={() => handleOpenTemplateDialog()}>
            Create Goal Template
          </Button>
        )}
      </Box>

      {isHR ? (
        <Paper>
          <Tabs value={tabValue} onChange={(_e, v) => setTabValue(v)}>
            <Tab label="Goal Templates" />
            <Tab label="Assignments" />
          </Tabs>
          <Box p={3}>
            {tabValue === 0 && (
              <Box>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Title</TableCell>
                        <TableCell>Description</TableCell>
                        <TableCell>Metric</TableCell>
                        <TableCell>Review Period</TableCell>
                        <TableCell align="right">Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {goalTemplates.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={5} align="center">
                            No goal templates. Create your first goal!
                          </TableCell>
                        </TableRow>
                      ) : (
                        goalTemplates.map((template) => (
                          <TableRow key={template.id}>
                            <TableCell>{template.title}</TableCell>
                            <TableCell>{template.description}</TableCell>
                            <TableCell>{template.metric}</TableCell>
                            <TableCell>{template.reviewPeriod}</TableCell>
                            <TableCell align="right">
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() => handleOpenAssignDialog(template)}
                                title="Assign to Employee"
                              >
                                <Assignment />
                              </IconButton>
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() => handleOpenTemplateDialog(template)}
                              >
                                <Edit />
                              </IconButton>
                              <IconButton
                                size="small"
                                color="error"
                                onClick={() => handleDeleteTemplate(template.id)}
                              >
                                <Delete />
                              </IconButton>
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}
            {tabValue === 1 && (
              <Box>
                <Typography variant="h6" gutterBottom>
                  Assigned Goals to Employees
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Employee</TableCell>
                        <TableCell>Goal Title</TableCell>
                        <TableCell>Description</TableCell>
                        <TableCell>Metric</TableCell>
                        <TableCell>Due Date</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Review Period</TableCell>
                        <TableCell align="right">Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {goals.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={8} align="center">
                            No assigned goals. Assign goals to employees via the "Goal Templates" tab
                          </TableCell>
                        </TableRow>
                      ) : (
                        goals.map((goal) => (
                          <TableRow key={goal.id}>
                            <TableCell>{goal.employeeName || `ID: ${goal.employeeId}`}</TableCell>
                            <TableCell>{goal.goalTitle}</TableCell>
                            <TableCell>{goal.goalDescription}</TableCell>
                            <TableCell>{goal.goalMetric}</TableCell>
                            <TableCell>{format(new Date(goal.dueDate), 'dd.MM.yyyy')}</TableCell>
                            <TableCell>
                              <Chip label={goal.status} color={getStatusColor(goal.status) as any} size="small" />
                            </TableCell>
                            <TableCell>{goal.reviewPeriod}</TableCell>
                            <TableCell align="right">
                              <IconButton
                                size="small"
                                color="error"
                                onClick={async () => {
                                  if (window.confirm('Are you sure you want to delete this assignment?')) {
                                    try {
                                      await goalService.deleteGoalAssignment(goal.id);
                                      fetchData();
                                    } catch (error) {
                                      console.error('Failed to delete assignment:', error);
                                      alert('Error deleting assignment');
                                    }
                                  }
                                }}
                              >
                                <Delete />
                              </IconButton>
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}
          </Box>
        </Paper>
      ) : (
        <Paper>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Title</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Metric</TableCell>
                  <TableCell>Due Date</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Review Period</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {goals.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      You have no assigned goals
                    </TableCell>
                  </TableRow>
                ) : (
                  goals.map((goal) => (
                    <TableRow key={goal.id}>
                      <TableCell>{goal.goalTitle}</TableCell>
                      <TableCell>{goal.goalDescription}</TableCell>
                      <TableCell>{goal.goalMetric}</TableCell>
                      <TableCell>{format(new Date(goal.dueDate), 'dd.MM.yyyy')}</TableCell>
                      <TableCell>
                        <Chip label={goal.status} color={getStatusColor(goal.status) as any} size="small" />
                      </TableCell>
                      <TableCell>{goal.reviewPeriod}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => handleOpenStatusDialog(goal)}
                          title="Change Status"
                        >
                          <Edit />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {/* Template Dialog */}
      <Dialog open={openTemplateDialog} onClose={handleCloseTemplateDialog} maxWidth="md" fullWidth>
        <DialogTitle>{editingTemplate ? 'Edit Goal Template' : 'Create Goal Template'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Title"
            value={templateFormData.title}
            onChange={(e) => setTemplateFormData({ ...templateFormData, title: e.target.value })}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="Description"
            value={templateFormData.description}
            onChange={(e) => setTemplateFormData({ ...templateFormData, description: e.target.value })}
            margin="normal"
            multiline
            rows={3}
            required
          />
          <TextField
            fullWidth
            label="Metric"
            value={templateFormData.metric}
            onChange={(e) => setTemplateFormData({ ...templateFormData, metric: e.target.value })}
            margin="normal"
            placeholder="e.g.: Complete 5 projects, Increase sales by 20%"
            required
          />
          <TextField
            fullWidth
            label="Review Period"
            value={templateFormData.reviewPeriod}
            onChange={(e) => setTemplateFormData({ ...templateFormData, reviewPeriod: e.target.value })}
            margin="normal"
            placeholder="e.g.: Q4-2024, 2024"
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseTemplateDialog}>Cancel</Button>
          <Button onClick={handleSubmitTemplate} variant="contained" color="primary">
            {editingTemplate ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Assign Dialog */}
      <Dialog open={openAssignDialog} onClose={handleCloseAssignDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Assign Goal to Employee</DialogTitle>
        <DialogContent>
          <FormControl fullWidth margin="normal">
            <InputLabel>Employee</InputLabel>
            <Select
              value={assignFormData.employeeId}
              onChange={(e) => setAssignFormData({ ...assignFormData, employeeId: Number(e.target.value) })}
              label="Employee"
            >
              {employees.map((emp) => (
                <MenuItem key={emp.id} value={emp.id}>
                  {emp.fullName}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label="Due Date"
            type="date"
            value={assignFormData.dueDate}
            onChange={(e) => setAssignFormData({ ...assignFormData, dueDate: e.target.value })}
            margin="normal"
            InputLabelProps={{ shrink: true }}
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseAssignDialog}>Cancel</Button>
          <Button onClick={handleSubmitAssign} variant="contained" color="primary">
            Assign
          </Button>
        </DialogActions>
      </Dialog>

      {/* Status Dialog */}
      <Dialog open={openStatusDialog} onClose={handleCloseStatusDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Change Goal Status</DialogTitle>
        <DialogContent>
          {selectedGoal && (
            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Goal: {selectedGoal.goalTitle}
              </Typography>
              <TextField
                fullWidth
                select
                label="Status"
                value={statusFormData.status}
                onChange={(e) => setStatusFormData({ status: e.target.value })}
                margin="normal"
                required
              >
                <MenuItem value="not_started">Not Started</MenuItem>
                <MenuItem value="in_progress">In Progress</MenuItem>
                <MenuItem value="blocked">Blocked</MenuItem>
                <MenuItem value="completed">Completed</MenuItem>
              </TextField>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseStatusDialog}>Cancel</Button>
          <Button onClick={handleSubmitStatus} variant="contained" color="primary">
            Update
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default GoalsPage;
